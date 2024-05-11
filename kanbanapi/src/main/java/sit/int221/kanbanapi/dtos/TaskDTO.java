package sit.int221.kanbanapi.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.entities.Status;

import java.time.OffsetDateTime;

@Getter
@Setter
public class TaskDTO {
    private Integer id;
    private String title;
    private String description;
    private String assignees;
    private String status;
    private OffsetDateTime createdOn;
    private OffsetDateTime updatedOn;
}
