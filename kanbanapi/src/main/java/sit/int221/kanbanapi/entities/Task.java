package sit.int221.kanbanapi.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import sit.int221.kanbanapi.repositories.StatusRepository;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taskId")
    private Integer id;

    @NotNull
    @NotEmpty
    @NotBlank
    @Size(max = 100)
    @Column(name = "taskTitle", nullable = false)
    private String title;

    @Size(max = 500)
    @Column(name = "taskDescription")
    private String description;

    @Size(max = 30)
    @Column(name = "taskAssignees")
    private String assignees;

    @ManyToOne
    @JoinColumn(name = "taskStatus")
    private Status taskStatus;

    @Column(name = "createdOn", insertable = false, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssXXX", timezone="UTC")
    private OffsetDateTime createdOn;

    @Column(name = "updatedOn", insertable = false, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssXXX", timezone="UTC")
    private OffsetDateTime updatedOn;

    public void setTitle(String title) {
        if (title != null) {
            title = title.trim();
            if (title.isBlank()) {
                title = null;
            }
        }
        this.title = title;
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

    public void setAssignees(String assignees) {
        if (assignees != null) {
            assignees = assignees.trim();
            if (assignees.isBlank()) {
                assignees = null;
            }
        }
        this.assignees = assignees;
    }

    public String getStatus() {
        return taskStatus.getName();
    }
}