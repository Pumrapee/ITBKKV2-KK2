package sit.int221.kanbanapi.databases.kanbandb.entities;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CollabId implements Serializable {
    private String boardId;
    private String userOid;
}