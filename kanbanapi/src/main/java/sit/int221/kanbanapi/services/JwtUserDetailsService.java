package sit.int221.kanbanapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sit.int221.kanbanapi.databases.userdb.entities.AuthUser;
import sit.int221.kanbanapi.databases.kanbandb.entities.MsUser;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.databases.kanbandb.repositories.MsUserRepository;
import sit.int221.kanbanapi.databases.userdb.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static sit.int221.kanbanapi.databases.userdb.entities.Role.MSUSER;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MsUserRepository msUserRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) {
        User user = userRepository.findByUsername(userName);
        if (user == null) { throw new UsernameNotFoundException(userName); }
        List<GrantedAuthority> roles = new ArrayList<>();
        GrantedAuthority grantedAuthority = () -> user.getRole().toString();
        roles.add(grantedAuthority);
        return new AuthUser(user.getUsername(), user.getPassword(), roles, user.getOid(), user.getName(), user.getEmail());
    }

    public UserDetails loadUserByOid(String Oid) {
        User user = userRepository.findById(Oid).orElse(null);
        if (user == null) {
            MsUser msUser = msUserRepository.findById(Oid).orElse(null);
            if (msUser == null) { throw new UsernameNotFoundException(Oid); }
            user = new User();
            user.setOid(msUser.getId());
            user.setEmail(msUser.getMail());
            user.setName(msUser.getDisplayName());
            user.setRole(MSUSER);
        }
        List<GrantedAuthority> roles = new ArrayList<>();
        User finalUser = user;
        GrantedAuthority grantedAuthority = () -> finalUser.getRole().toString();
        roles.add(grantedAuthority);
        return new AuthUser(user.getUsername(), user.getPassword(), roles, user.getOid(), user.getName(), user.getEmail());
    }

    public AuthUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            return (AuthUser) authentication.getPrincipal();
        }

        return null;
    }

}

