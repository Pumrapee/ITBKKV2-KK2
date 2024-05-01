package sit.int221.kanbanapi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.models.TaskStatus;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String description;
    private String assignees;
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.NO_STATUS;
    @Column(insertable = false, updatable = false)
    private LocalDateTime createdOn;
    @Column(insertable = false, updatable = false)
    private LocalDateTime updatedOn;
}
