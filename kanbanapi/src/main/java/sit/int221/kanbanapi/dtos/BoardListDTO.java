package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.databases.kanbandb.entities.Collab;

import java.time.OffsetDateTime;

@Getter
@Setter
public class BoardListDTO {
    private String id;
    private String name;
    private String visibility;
    private String accessRight;
    private Collab.Status status;
    private OffsetDateTime createdOn;
    private Owner owner;
}
