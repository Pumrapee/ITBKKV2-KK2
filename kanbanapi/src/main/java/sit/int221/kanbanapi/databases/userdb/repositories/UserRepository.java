package sit.int221.kanbanapi.databases.userdb.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.kanbanapi.databases.userdb.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}
