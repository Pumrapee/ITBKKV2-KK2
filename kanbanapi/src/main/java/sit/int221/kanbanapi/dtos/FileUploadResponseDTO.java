package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.databases.kanbandb.entities.Attachment;

import java.util.List;

@Getter
@Setter
public class FileUploadResponseDTO {
    private List<String> addedFiles;
    private List<String> existingFiles;
    private List<String> notAddedFiles;
}
