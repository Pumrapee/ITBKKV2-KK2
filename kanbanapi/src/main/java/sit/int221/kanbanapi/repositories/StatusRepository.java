package sit.int221.kanbanapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.kanbanapi.entities.Status;

import java.util.Optional;

public interface StatusRepository extends JpaRepository <Status, Integer> {
    Optional<Status> findByName(String name);

    boolean existsByName(String statusName);
}
