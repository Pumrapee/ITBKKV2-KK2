package sit.int221.kanbanapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.kanbanapi.entities.Status;

import java.util.Optional;

public interface StatusRepository extends JpaRepository <Status, Integer> {
    Optional<Status> findByName(String name);
    Boolean existsByName(String statusName);
    Boolean existsByNameAndIdNot(String name, Integer id);
}
