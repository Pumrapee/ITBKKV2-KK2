package sit.int221.kanbanapi.databases.kanbandb.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String boardId;

    @Id
    private String userOid;

    @Enumerated(EnumType.STRING)
    private AccessRight accessRight = AccessRight.READ;

    @Column(name = "addedOn", insertable = false, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssXXX", timezone="UTC")
    private OffsetDateTime addedOn;

    public enum AccessRight {
        READ, WRITE
    }
}