package sit.int221.kanbanapi.databases.kanbandb.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    public List<Board> findByOwnerId(String oid);
}
