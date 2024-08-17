package sit.int221.kanbanapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.databases.userdb.repositories.UserRepository;
import sit.int221.kanbanapi.exceptions.AuthenticationFailed;


@Service
public class AuthenticationService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public boolean authenticate(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AuthenticationFailed("username or password incorrect"));
        return passwordEncoder.matches(password, user.getPassword());
    }
}
