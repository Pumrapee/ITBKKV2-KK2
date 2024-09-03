package sit.int221.kanbanapi.databases.kanbandb.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.kanbanapi.databases.kanbandb.entities.Status;

import java.util.Optional;

public interface StatusRepository extends JpaRepository <Status, Integer> {
    Optional<Status> findByName(String name);
    Boolean existsByName(String statusName);
    Boolean existsByNameAndIdNot(String name, Integer id);
}
