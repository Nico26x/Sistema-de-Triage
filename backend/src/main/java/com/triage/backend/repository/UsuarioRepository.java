package com.triage.backend.repository;

import com.triage.backend.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByIdentificacion(String identificacion);

    boolean existsByEmail(String email);

    boolean existsByIdentificacion(String identificacion);
}