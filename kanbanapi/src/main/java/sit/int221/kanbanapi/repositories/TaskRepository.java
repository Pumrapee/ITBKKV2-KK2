package sit.int221.kanbanapi.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sit.int221.kanbanapi.entities.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status.id = :newId WHERE t.status.id = :id")
    void transferTaskStatus(Integer id, Integer newId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status.id = :statusId")
    Integer countTasksByStatus(Integer statusId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status.name != 'No Status' and t.status.name != 'Done' GROUP BY t.status.id")
    List<Integer> countTasksByStatus();

    @Query("SELECT t FROM Task t JOIN t.status s WHERE s.name IN :filterStatusNames")
    List<Task> findByStatusNamesSorted(List<String> filterStatusNames, Sort sort);

    @Query("SELECT t FROM Task t JOIN t.status s WHERE s.id IN :filterStatusIds OR s.name IN :filterStatusNames")
    List<Task> findByStatusIdsAndNamesSorted(List<Integer> filterStatusIds, List<String> filterStatusNames, Sort sort);

}
