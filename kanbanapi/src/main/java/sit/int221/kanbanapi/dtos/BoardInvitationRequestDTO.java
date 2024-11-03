package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.databases.kanbandb.entities.Collab;
import sit.int221.kanbanapi.validators.ValidEnum;

@Getter
@Setter
public class BoardInvitationRequestDTO {
    @ValidEnum(enumClass = Invitation.class, message = "Status must be either 'ACCEPT' or 'DECLINE'.")
    private String invitation;

    public Invitation getInvitation() {
        return Invitation.valueOf(invitation.toUpperCase());
    }
}
