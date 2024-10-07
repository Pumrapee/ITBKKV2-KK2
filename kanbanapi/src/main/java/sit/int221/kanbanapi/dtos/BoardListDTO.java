package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardListDTO {
    private String id;
    private String name;
    private String visibility;
    private String role;
    private String accessRight;
    private Owner owner;
}
