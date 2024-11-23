package sit.int221.kanbanapi.databases.kanbandb.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.kanbanapi.databases.kanbandb.entities.Attachment;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {
    List<Attachment> findByTaskId(Integer taskId);
    Optional<Attachment> findByIdAndTaskId(Integer id, Integer taskId);
}