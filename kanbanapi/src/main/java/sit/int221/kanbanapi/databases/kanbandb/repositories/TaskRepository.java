package sit.int221.kanbanapi.databases.kanbandb.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.kanbandb.entities.Task;

import java.util.List;
public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.taskStatus.id = :newId WHERE t.taskStatus.id = :id AND t.board = :board")
    void transferTaskStatus(Integer id, Integer newId, Board board);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.taskStatus.id = :statusId AND t.board = :board")
    Integer countTasksByStatus(Integer statusId,Board board);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.taskStatus.name != 'No Status' and t.taskStatus.name != 'Done' AND t.board = :board GROUP BY t.taskStatus.id")
    List<Integer> countTasksByStatus(Board board);

    @Query("SELECT t FROM Task t JOIN t.taskStatus s WHERE s.name IN :filterStatusNames AND t.board = :board")
    List<Task> findByStatusNamesSorted(List<String> filterStatusNames,Board board, Sort sort);

    @Query("SELECT t FROM Task t JOIN t.taskStatus s WHERE (s.id IN :filterStatusIds OR s.name IN :filterStatusNames) AND t.board = :board")
    List<Task> findByStatusIdsAndNamesSorted(List<Integer> filterStatusIds,List<String> filterStatusNames,Board board, Sort sort);

    @Query("SELECT t FROM Task t WHERE t.board = :board")
    List<Task> findByBoardSorted(Board board, Sort sort);
}
