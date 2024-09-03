package sit.int221.kanbanapi.databases.userdb.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.kanbanapi.databases.userdb.entities.User;


public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}
