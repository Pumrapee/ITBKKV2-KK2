package sit.int221.kanbanapi.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusDTO {
    private Integer id;
    private String name;
    private String description;
}
