package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.models.TaskStatus;

@Getter
@Setter
public class TaskCreateUpdateDTO {
    private Integer id;
    private String title;
    private String description;
    private String assignees;
    private TaskStatus status;
}
