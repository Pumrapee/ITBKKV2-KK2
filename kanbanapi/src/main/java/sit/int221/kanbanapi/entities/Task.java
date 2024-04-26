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
    private Integer taskId;
    private String taskTitle;
    private String taskDescription;
    private String taskAssignees;
    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
