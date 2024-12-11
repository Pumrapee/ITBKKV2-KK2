package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class SimpleTaskDTO {
    private Integer id;
    private String title;
    private String assignees;
    private String status;
    private Integer attachmentCount;
}
