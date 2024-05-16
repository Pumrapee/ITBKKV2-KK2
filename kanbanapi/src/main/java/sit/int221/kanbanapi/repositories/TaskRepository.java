package sit.int221.kanbanapi.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int221.kanbanapi.entities.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status.id = :newId WHERE t.status.id = :id")
    void transferTaskStatus(Integer id, Integer newId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status.id = :statusId")
    Integer existsByStatus(Integer statusId);

    @Query("SELECT t FROM Task t JOIN t.status s WHERE s.name IN :filterStatuses")
    List<Task> findByStatusNamesSorted(@Param("filterStatuses") List<String> filterStatuses, Sort sort);
}
