package com.haal.backend.auth.security;

import com.haal.backend.auth.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class HaalUserDetails implements UserDetails {

    private final User user;
    public UUID getUserId() {
        return user.getId();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    // No password stored in UserDetails — phone hash is checked at login only
    @Override public String getPassword()   { return null; }
    @Override public String getUsername()   { return user.getId().toString(); }
    @Override public boolean isEnabled()    { return user.isActive(); }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
}
