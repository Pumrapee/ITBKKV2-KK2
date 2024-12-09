package sit.int221.kanbanapi.databases.kanbandb.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.kanbanapi.databases.kanbandb.entities.MsUser;

import java.util.Optional;


public interface MsUserRepository extends JpaRepository<MsUser, String> {
    Optional<MsUser> findByMail(String mail);
}
