package sit.int221.kanbanapi.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.models.TaskStatus;

import java.sql.Date;


@Getter
@Setter
public class TaskDTO {
    private Integer id;
    private String title;
    private String description;
    private String assignees;
    private TaskStatus status;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssXXX", timezone="UTC")
    private Date createdOn;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssXXX", timezone="UTC")
    private Date updatedOn;
}
