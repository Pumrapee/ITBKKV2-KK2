package sit.int221.kanbanapi.databases.kanbandb.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.kanbanapi.databases.kanbandb.entities.Collab;
import sit.int221.kanbanapi.databases.kanbandb.entities.CollabId;

import java.util.List;
import java.util.Optional;

public interface CollabRepository extends JpaRepository<Collab, CollabId> {
    List<Collab> findByBoardId(String boardId);
    Optional<Collab> findByBoardIdAndUserOid(String boardId, String userOid);
    List<Collab> findByUserOid(String userOid);
}