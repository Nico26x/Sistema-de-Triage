package com.triage.backend.repository;

import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.RolNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findByIdentificacion(String identificacion);
    
    List<Usuario> findByActivoTrue();
    
    List<Usuario> findByRol(RolNombre rol);
}