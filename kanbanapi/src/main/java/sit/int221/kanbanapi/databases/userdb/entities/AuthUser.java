package sit.int221.kanbanapi.databases.userdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;

@Getter
@Setter
public class AuthUser implements UserDetails, Serializable {

    private final String userName;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String userOid;
    private final String name;
    private final String email;

    public AuthUser(String userName, String password, Collection<? extends GrantedAuthority> authorities, String userOid, String name, String email) {
        this.userName = userName;
        this.password = password;
        this.authorities = authorities;
        this.userOid = userOid;
        this.name = name;
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}