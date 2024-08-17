package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;

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
