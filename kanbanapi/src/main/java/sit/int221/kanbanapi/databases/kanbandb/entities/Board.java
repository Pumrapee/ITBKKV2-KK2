package sit.int221.kanbanapi.databases.kanbandb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "boards")
@Getter
@Setter
public class Board {

    @Id
    @Column(name = "boardId", length = 10)
    private String boardId;

    @Column(name = "boardName", nullable = false, length = 100)
    private String boardName;

    @Column(name = "owneroid", nullable = false, length = 36)
    private String ownerId;

    @JsonIgnore
    @OneToMany(mappedBy = "board")
    private List<Status> statuses;

    @JsonIgnore
    @OneToMany(mappedBy = "board")
    private List<Task> tasks;

}
