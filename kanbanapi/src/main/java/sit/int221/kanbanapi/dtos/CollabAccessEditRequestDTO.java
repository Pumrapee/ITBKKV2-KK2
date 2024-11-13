package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.databases.kanbandb.entities.Collab;
import sit.int221.kanbanapi.validators.ValidEnum;

@Getter
@Setter
public class CollabAccessEditRequestDTO {
    @ValidEnum(enumClass = Collab.AccessRight.class, message = "access_right must be either 'READ' or 'WRITE'.")
    private String accessRight;

    public Collab.AccessRight getAccess_right() {
        return Collab.AccessRight.valueOf(accessRight.toUpperCase());
    }
}
