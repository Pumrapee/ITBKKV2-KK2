package sit.int221.kanbanapi.services;

import jakarta.annotation.Resource;
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
import sit.int221.kanbanapi.exceptions.ItemNotFoundException;
import sit.int221.kanbanapi.exceptions.TaskLimitExceededException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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
        // Fetch current attachments
        List<Attachment> existingAttachments = getAttachmentsByTaskId(taskId);
        if (existingAttachments.size() >= MAX_FILES) {
            throw new TaskLimitExceededException("Each task can have at most " + MAX_FILES + " files.");
        }

        // Validate and process each file
        List<String> addedFiles = new ArrayList<>();
        List<String> existingFiles = new ArrayList<>();
        List<String> notAddedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (existingAttachments.size() >= MAX_FILES) {
                notAddedFiles.add(file.getOriginalFilename());
                continue;
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                notAddedFiles.add(file.getOriginalFilename());
                continue;
            }

            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            if (existingAttachments.stream().anyMatch(att -> att.getFilename().equals(filename))) {
                // Skip duplicate filenames within the task
                existingFiles.add(file.getOriginalFilename());
                continue;
            }

            try {
                String filePath = Paths.get(uploadDir, filename).toString();
                Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
                Attachment attachment = new Attachment();
                attachment.setFilename(filename);
                attachment.setFile_path(filePath);
                attachment.setTask(taskRepository.findById(taskId).orElseThrow(() -> new ItemNotFoundException("Task " + taskId + " does not exist !!!")));
                attachmentRepository.save(attachment);
                addedFiles.add(file.getOriginalFilename());
            } catch (IOException ex) {
                throw new RuntimeException("Could not store file " + filename, ex);
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
}
