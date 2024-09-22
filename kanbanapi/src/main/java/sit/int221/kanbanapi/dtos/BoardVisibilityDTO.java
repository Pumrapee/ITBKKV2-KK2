package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.validators.ValidEnum;

@Getter
@Setter
public class BoardVisibilityDTO {

    @ValidEnum(enumClass = Board.Visibility.class, message = "Visibility must be either 'PRIVATE' or 'PUBLIC'.")
    private String visibility;

    public Board.Visibility getVisibility() {
        return Board.Visibility.valueOf(visibility.toUpperCase());
    }
}
