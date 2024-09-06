package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardTaskLimitDTO {
    private Boolean taskLimitEnabled;
    private Integer maxTasksPerStatus;
}
