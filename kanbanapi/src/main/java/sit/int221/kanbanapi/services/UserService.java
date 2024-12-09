package sit.int221.kanbanapi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int221.kanbanapi.databases.kanbandb.entities.MsUser;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.databases.kanbandb.repositories.MsUserRepository;
import sit.int221.kanbanapi.databases.userdb.repositories.UserRepository;
import sit.int221.kanbanapi.exceptions.BadRequestException;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    MsUserRepository msUserRepository;

    public User getUserById(String oid) {
        User user = userRepository.findById(oid).orElse(null);
        if (user == null) {
            MsUser msUser = msUserRepository.findById(oid).orElseThrow(() -> new BadRequestException("User "+ oid + " does not exist"));
            user = new User(msUser.getId(), msUser.getDisplayName(), msUser.getMail());
        }
        return user;
    }

    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            MsUser msUser = msUserRepository.findByMail(email).orElseThrow(() -> new BadRequestException("Email "+ email + " does not exist"));
            user = new User(msUser.getId(), msUser.getDisplayName(), msUser.getMail());
        }
        return user;
    }

    @Transactional
    public MsUser msUser(MsUser user) {
        if (userRepository.existsById(user.getId())) {
            return user;
        } else {
            return msUserRepository.save(user);
        }
    }
}
