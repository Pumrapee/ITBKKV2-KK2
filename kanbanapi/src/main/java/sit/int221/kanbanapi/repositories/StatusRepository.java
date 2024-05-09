package sit.int221.kanbanapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.kanbanapi.entities.Status;

public interface StatusRepository extends JpaRepository <Status, Integer> {
    Status findByName(String name);
}
