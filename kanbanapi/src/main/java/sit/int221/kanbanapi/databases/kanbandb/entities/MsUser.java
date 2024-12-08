package sit.int221.kanbanapi.databases.kanbandb.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "msusers")
public class MsUser {
    @Id
    private String id;
    private String mail;
    private String displayName;
}
