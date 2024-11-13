package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BoardAndCollabBoardListDTO {
    private List<BoardListDTO> owner;
    private List<BoardListDTO> collab;
}
