package sit.int221.kanbanapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BoardResponseDTO {
    private String id;
    private String name;
    private Owner owner;
}
