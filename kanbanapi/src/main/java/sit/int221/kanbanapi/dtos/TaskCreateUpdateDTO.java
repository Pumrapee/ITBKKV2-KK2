package sit.int221.kanbanapi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TaskCreateUpdateDTO {
    private Integer id;

    @NotNull(message = "Task title cannot be null")
    @NotBlank(message = "Task title cannot be blank")
    @Size(max = 100, message = "Task title must be at most 100 characters long")
    private String title;

    @Size(max = 500, message = "Task description must be at most 500 characters long")
    private String description;

    @Size(max = 30, message = "Task assignees must be at most 30 characters long")
    private String assignees;
    private String status;
}
