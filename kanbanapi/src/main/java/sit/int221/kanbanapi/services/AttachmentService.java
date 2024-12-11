package sit.int221.kanbanapi.services;

import jakarta.annotation.Resource;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.kanbanapi.databases.kanbandb.entities.Attachment;
import sit.int221.kanbanapi.databases.kanbandb.entities.Task;
import sit.int221.kanbanapi.databases.kanbandb.repositories.AttachmentRepository;
import sit.int221.kanbanapi.databases.kanbandb.repositories.TaskRepository;
import sit.int221.kanbanapi.dtos.FileUploadResponseDTO;
import sit.int221.kanbanapi.exceptions.BadRequestException;
import sit.int221.kanbanapi.exceptions.ItemNotFoundException;
import sit.int221.kanbanapi.exceptions.TaskLimitExceededException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AttachmentService {
    private static final int MAX_FILES = 10;
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20 MB

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private TaskRepository taskRepository;

    public List<Attachment> getAttachmentsByTaskId(Integer taskId) {
        return attachmentRepository.findByTaskId(taskId);
    }

    public Attachment getAttachment(Integer taskId, Integer attachmentId) {
        return attachmentRepository.findByIdAndTaskId(attachmentId, taskId)
                .orElseThrow(() -> new ItemNotFoundException("Attachment not found for the given ID and task."));
    }

    public FileUploadResponseDTO uploadAttachments(Integer taskId, MultipartFile[] files) {
        Path taskUploadDir = Paths.get(uploadDir, String.valueOf(taskId)).normalize();

        try {
            // Ensure the directory exists
            if (!Files.exists(taskUploadDir)) {
                Files.createDirectories(taskUploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory for task " + taskId, e);
        }

        // Fetch current attachments
        List<Attachment> existingAttachments = getAttachmentsByTaskId(taskId);
        if (existingAttachments.size() >= MAX_FILES) {
            throw new TaskLimitExceededException("Each task can have at most " + MAX_FILES + " files.");
        }

        List<String> addedFiles = new ArrayList<>();
        List<String> existingFiles = new ArrayList<>();
        List<String> notAddedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());

            if (filename.contains("..")) {
                notAddedFiles.add(filename);
                continue;
            }

            if (existingAttachments.stream().anyMatch(att -> att.getFilename().equals(filename))) {
                existingFiles.add(filename);
                continue;
            }

            if (existingAttachments.size() + addedFiles.size() >= MAX_FILES) {
                notAddedFiles.add(filename);
                continue;
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                notAddedFiles.add(filename);
                continue;
            }

            Path targetPath = taskUploadDir.resolve(filename).normalize();

            // Ensure the target path is within the intended directory
            if (!targetPath.startsWith(taskUploadDir)) {
                notAddedFiles.add(filename);
                continue;
            }

            try {
                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                Attachment attachment = new Attachment();
                attachment.setFilename(filename);
                attachment.setFile_path(targetPath.toString());
                attachment.setTask(taskRepository.findById(taskId)
                        .orElseThrow(() -> new ItemNotFoundException("Task " + taskId + " does not exist !!!")));
                attachmentRepository.save(attachment);
                addedFiles.add(filename);
            } catch (IOException ex) {
                notAddedFiles.add(filename);
            }
        }

        FileUploadResponseDTO fileUploadResponseDTO = new FileUploadResponseDTO();
        fileUploadResponseDTO.setAddedFiles(addedFiles);
        fileUploadResponseDTO.setExistingFiles(existingFiles);
        fileUploadResponseDTO.setNotAddedFiles(notAddedFiles);
        return fileUploadResponseDTO;
    }

    public void deleteAttachment(Integer attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ItemNotFoundException("Attachment not found"));
        attachmentRepository.delete(attachment);

        // Delete the physical file
        try {
            Files.deleteIfExists(Paths.get(attachment.getFile_path()));
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + attachment.getFile_path(), ex);
        }
    }

    public void deleteAttachmentDir(Integer taskId) {
        java.io.File taskDirectory = new java.io.File(uploadDir + "/" + taskId);

        if (taskDirectory.exists()) {
            try {
                // Use Files.walk to traverse and delete all files and subdirectories
                Files.walk(taskDirectory.toPath())
                        .sorted(Comparator.reverseOrder()) // Delete files and directories in reverse order
                        .map(Path::toFile)
                        .forEach(java.io.File::delete);
            } catch (IOException ex) {
                throw new RuntimeException("Could not delete task directory: " + taskDirectory.getPath(), ex);
            }
        }
    }

    public byte[] generateThumbnail(Attachment attachment) {
        try {
            // Load the original file
            Path originalFilePath = Paths.get(attachment.getFile_path());
            BufferedImage originalImage = ImageIO.read(originalFilePath.toFile());

            // Generate a thumbnail in memory
            BufferedImage thumbnailImage = Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, 150, 150);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(thumbnailImage, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new BadRequestException("Could not generate thumbnail for file " + attachment.getFilename());
        }
    }

}
