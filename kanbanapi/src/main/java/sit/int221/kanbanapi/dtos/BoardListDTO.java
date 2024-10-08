package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

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
}
