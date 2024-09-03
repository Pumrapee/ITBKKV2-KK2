package sit.int221.kanbanapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Owner {
    private String oid;
    private String name;
}
