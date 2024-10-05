package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollabAddRespondDTO {
    private String boardId;
    private String collaboratorName;
    private String collaboratorEmail;
    private String accessRight;
}
