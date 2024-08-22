package sit.int221.kanbanapi.databases.userdb.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@Setter
@Entity
@Table(name = "users")
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
}
