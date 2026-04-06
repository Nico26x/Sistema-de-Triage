package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.RolNombre;

public record RegisterRequestDTO(
    String nombre,
    String email,
    String identificacion,
    String password,
    RolNombre rol
) {}