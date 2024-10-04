package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class CollaboratorDTO {
    private String oid;
    private String name;
    private String email;
    private String accessRight;
    private OffsetDateTime addedOn;
}

