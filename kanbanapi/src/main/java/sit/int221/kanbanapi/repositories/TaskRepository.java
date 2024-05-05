package sit.int221.kanbanapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.kanbanapi.entities.Task;

public interface TaskRepository extends JpaRepository<Task, Integer> {
}
