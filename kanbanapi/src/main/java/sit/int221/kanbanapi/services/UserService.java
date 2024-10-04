package sit.int221.kanbanapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.databases.userdb.repositories.UserRepository;
import sit.int221.kanbanapi.exceptions.BadRequestException;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    public User getUserById(String oid) {
        return userRepository.findById(oid).orElseThrow(() -> new BadRequestException("User "+ oid + " does not exist"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("User "+ email + " does not exist"));
    }
}
