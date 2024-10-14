package sit.int221.kanbanapi.databases.kanbandb.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "collabs")
@IdClass(CollabId.class)
public class Collab {

    @Id
    @JsonIgnore
    private String boardId;

    @Id
    private String userOid;

    @Enumerated(EnumType.STRING)
    private AccessRight accessRight = AccessRight.READ;

    @Column(name = "addedOn", insertable = false, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssXXX", timezone="UTC")
    private OffsetDateTime addedOn;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "boardId", insertable = false, updatable = false)
    private Board collabBoard;

    public enum AccessRight {
        READ, WRITE
    }
}