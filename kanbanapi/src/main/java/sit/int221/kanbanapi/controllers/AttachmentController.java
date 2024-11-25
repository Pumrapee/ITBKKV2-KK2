package sit.int221.kanbanapi.controllers;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.kanbanapi.databases.kanbandb.entities.Attachment;
import sit.int221.kanbanapi.dtos.FileUploadResponseDTO;
import sit.int221.kanbanapi.services.AttachmentService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/v3/boards/{boardId}/tasks/{taskId}/attachments")
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th", "http://localhost:5173", "http://intproj23.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})
public class AttachmentController {
    @Autowired
    private AttachmentService attachmentService;

    @GetMapping
    public ResponseEntity<List<Attachment>> getAttachments(@PathVariable Integer taskId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByTaskId(taskId));
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<UrlResource> downloadAttachment(
            @PathVariable Integer taskId,
            @PathVariable Integer attachmentId) {
        // Call service to get attachment details
        Attachment attachment = attachmentService.getAttachment(taskId, attachmentId);

        // Load the file as a resource
        Path filePath = Paths.get(attachment.getFile_path());
        UrlResource resource;
        try {
            resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error while reading the file: " + e.getMessage());
        }

        String contentType;
        try {
            contentType = Files.probeContentType(filePath); // Detects file type (e.g., image/png, application/pdf)
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
        } catch (IOException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping
    public ResponseEntity<FileUploadResponseDTO> uploadAttachments(
            @PathVariable Integer taskId,
            @RequestParam("files") MultipartFile[] files) {
        return ResponseEntity.ok().body(attachmentService.uploadAttachments(taskId, files));
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Integer attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.ok().build();
    }
}

