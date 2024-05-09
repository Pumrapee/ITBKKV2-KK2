package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TaskCreateUpdateDTO {
    private Integer id;
    private String title;
    private String description;
    private String assignees;
    private String status;
}
