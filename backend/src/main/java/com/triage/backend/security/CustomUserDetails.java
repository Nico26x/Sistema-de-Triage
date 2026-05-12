package com.triage.backend.security;

import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.RolNombre;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mapear RolNombre a GrantedAuthority con prefijo ROLE_
        RolNombre rol = usuario.getRol();
        String roleName = "ROLE_" + rol.name();
        return Collections.singleton(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return usuario.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
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
        return usuario.isActivo();
    }

    // Getters adicionales para acceder a datos del usuario
    public Long getUsuarioId() {
        return usuario.getId();
    }

    public String getEmail() {
        return usuario.getEmail();
    }

    public RolNombre getRol() {
        return usuario.getRol();
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
