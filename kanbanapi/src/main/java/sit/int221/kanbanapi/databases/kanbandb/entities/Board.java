package sit.int221.kanbanapi.databases.kanbandb.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.databases.userdb.entities.User;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "boards")
@Getter
@Setter
public class Board {


    public enum Visibility {
        PRIVATE, PUBLIC
    }

    @Id
    @Column(name = "boardId", length = 10)
    private String boardId;

    @Column(name = "boardName", nullable = false, length = 120)
    private String boardName;

    @JsonIgnore
    @Column(name = "owneroid", nullable = false, length = 36)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(name = "taskLimitEnabled", nullable = false)
    private Boolean taskLimitEnabled;

    @Column(name = "maxTasksPerStatus", nullable = false)
    private Integer maxTasksPerStatus;

    @JsonIgnore
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<Task> tasks;

    @JsonIgnore
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<Status> statuses;

    @JsonIgnore
    @OneToMany(mappedBy = "collabBoard", cascade = CascadeType.ALL)
    private List<Collab> collabs;

    @Column(name = "createdOn", insertable = false, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssXXX", timezone="UTC")
    private OffsetDateTime createdOn;

    public String getId() {
        return boardId;
    }

    public String getName() {
        return boardName;
    }

    public String getVisibility() {
        return visibility.toString();
    }
}
