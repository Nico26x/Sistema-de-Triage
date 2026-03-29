package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.RolNombre;

import com.triage.backend.domain.enums.RolNombre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {
    private String nombre;
    private String email;
    private String identificacion;
    private String password;
    private RolNombre rol;
}