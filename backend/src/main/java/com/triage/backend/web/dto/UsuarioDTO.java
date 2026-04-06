package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.RolNombre;

public record UsuarioDTO(
    Long id,
    String nombre,
    String email,
    String identificacion,
    boolean activo,
    RolNombre rol
) {}
