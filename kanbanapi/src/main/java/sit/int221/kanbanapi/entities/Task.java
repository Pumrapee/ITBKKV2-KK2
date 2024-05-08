package sit.int221.kanbanapi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.models.TaskStatus;

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
    @Column(name = "taskTitle")
    private String title;
    @Column(name = "taskDescription")
    private String description;
    @Column(name = "taskAssignees")
    private String assignees;
    @Enumerated(EnumType.STRING)
    @Column(name = "taskStatus")
    private TaskStatus status = TaskStatus.NO_STATUS;
    @Column(name = "createdOn", insertable = false, updatable = false)
    private OffsetDateTime createdOn;
    @Column(name = "updatedOn", insertable = false, updatable = false)
    private OffsetDateTime updatedOn;

    public void setTitle(String title) {
        if (title != null) {
            title = title.trim();
        }
        this.title = title;
    }

    public void setDescription(String description) {
        if (description != null) {
            description = description.trim();
        }
        if (description.isEmpty()) {
            description = null;
        }
        this.description = description;
    }

    public void setAssignees(String assignees) {
        if (assignees != null) {
            assignees = assignees.trim();
        }
        if (assignees.isEmpty()) {
            assignees = null;
        }
        this.assignees = assignees;
    }

    public void setStatus(TaskStatus status) {
        if (status == null) {
            status = TaskStatus.NO_STATUS;
        }
        this.status = status;
    }
}
