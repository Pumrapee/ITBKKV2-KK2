package sit.int221.kanbanapi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "statuses")
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statusId")
    private Integer id;
    @Column(name = "statusName", nullable = false, unique = true)
    private String name;
    @Column(name = "statusDescription")
    private String description;

    public void setName(String name) {
        if (name != null) {
            name = name.trim();
            if (name.isBlank()) {
                name = null;
            }
        }
        this.name = name;
    }

    public void setDescription(String description) {
        if (description != null) {
            description = description.trim();
            if (description.isBlank()) {
                description = null;
            }
        }
        this.description = description;
    }
}
