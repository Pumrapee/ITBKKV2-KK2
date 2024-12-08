package sit.int221.kanbanapi.databases.userdb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "oid", length = 36, nullable = false, unique = true)
    private String oid;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "email", length = 50, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.STUDENT;

    public String getCollaboratorName() {
        return name;
    }

    public String getCollaboratorEmail() {
        return email;
    }

    public User(String oid,String name, String email) {
        this.oid = oid;
        this.name = name;
        this.email = email;
    }
}
