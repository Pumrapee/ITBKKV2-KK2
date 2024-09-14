package sit.int221.kanbanapi.databases.kanbandb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "statuses")
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statusId")
    private Integer id;

    @NotNull
    @NotBlank
    @Size(max = 50)
    @Column(name = "statusName", unique = true, nullable = false)
    private String name;

    @Size(max = 200)
    @Column(name = "statusDescription")
    private String description;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "must be a valid hex color code")
    @Column(name = "statusColor", nullable = false)
    private String color = "#ffffff";

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "boardId", nullable = false)
    private Board board;

    @JsonIgnore
    @OneToMany(mappedBy = "taskStatus")
    private List<Task> tasks;

    public Status(String name, String description, String color, Board board) {
        this.name = name;
        this.description = description;
        this.color = (color == null || color.isBlank()) ? "#ffffff" : color;
        this.board = board;
    }

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

    public void setColor(String color) {
        if (color == null || color.isBlank() || color.length() != 7) {
            color = "#ffffff";
        }
        this.color = color;
    }
}
