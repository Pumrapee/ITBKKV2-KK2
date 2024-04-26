package sit.int221.kanbanapi.entities;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sit.int221.kanbanapi.models.TaskStatus;

import java.util.Date;


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
    private Date createdOn;
    private Date updatedOn;
}
