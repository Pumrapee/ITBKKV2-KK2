package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.databases.kanbandb.entities.Collab;

@Getter
@Setter
public class CollabAddRespondDTO {
    private String boardId;
    private String collaboratorName;
    private String collaboratorEmail;
    private String accessRight;
    private Collab.Status status;
}
