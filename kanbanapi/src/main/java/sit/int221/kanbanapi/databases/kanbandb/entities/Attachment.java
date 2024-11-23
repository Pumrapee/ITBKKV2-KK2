package sit.int221.kanbanapi.databases.kanbandb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "attachments")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String filename;

    @JsonIgnore
    @Column(nullable = false)
    private String file_path;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "taskId", nullable = false)
    private Task task;
}