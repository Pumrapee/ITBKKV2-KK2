package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.databases.kanbandb.entities.Collab;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class BoardListDTO {
    private String id;
    private String name;
    private String visibility;
    private String role;
    private String accessRight;
    private OffsetDateTime createdOn;
    private Owner owner;
    private List<Collab> collabs;
}
