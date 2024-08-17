package sit.int221.kanbanapi.databases.kanbandb.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import sit.int221.kanbanapi.databases.kanbandb.entities.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.taskStatus.id = :newId WHERE t.taskStatus.id = :id")
    void transferTaskStatus(Integer id, Integer newId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.taskStatus.id = :statusId")
    Integer countTasksByStatus(Integer statusId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.taskStatus.name != 'No Status' and t.taskStatus.name != 'Done' GROUP BY t.taskStatus.id")
    List<Integer> countTasksByStatus();

    @Query("SELECT t FROM Task t JOIN t.taskStatus s WHERE s.name IN :filterStatusNames")
    List<Task> findByStatusNamesSorted(List<String> filterStatusNames, Sort sort);

    @Query("SELECT t FROM Task t JOIN t.taskStatus s WHERE s.id IN :filterStatusIds OR s.name IN :filterStatusNames")
    List<Task> findByStatusIdsAndNamesSorted(List<Integer> filterStatusIds, List<String> filterStatusNames, Sort sort);

}
