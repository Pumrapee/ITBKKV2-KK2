package sit.int221.kanbanapi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.models.TaskStatus;

import java.time.LocalDateTime;
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
}
