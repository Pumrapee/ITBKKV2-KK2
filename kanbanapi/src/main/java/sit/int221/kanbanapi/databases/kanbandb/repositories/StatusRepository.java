package sit.int221.kanbanapi.databases.kanbandb.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.kanbandb.entities.Status;

import java.util.List;
import java.util.Optional;

public interface StatusRepository extends JpaRepository <Status, Integer> {
    Optional<Status> findByNameAndBoard(String name, Board board);
    Boolean existsByNameAndBoard(String name, Board board);
    Boolean existsByNameAndIdNotAndBoard(String name, Integer id, Board board);
    @Query("SELECT s FROM Status s WHERE s.board = :board OR s.name IN :names")
    List<Status> findAllByBoardOrNameIn(Board board, List<String> names);
}
