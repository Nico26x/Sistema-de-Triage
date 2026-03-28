package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.RolNombre;

public class AuthResponseDTO {
    private String token; // PROVISIONAL (no JWT)
    private String tipo;  // "Provisional"
    private Long userId;
    private String email;
    private RolNombre rol;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, String tipo, Long userId, String email, RolNombre rol) {
        this.token = token;
        this.tipo = tipo;
        this.userId = userId;
        this.email = email;
        this.rol = rol;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public RolNombre getRol() { return rol; }
    public void setRol(RolNombre rol) { this.rol = rol; }
}