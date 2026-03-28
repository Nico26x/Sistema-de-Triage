package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.RolNombre;

public class RegisterRequestDTO {
    private String nombre;
    private String email;
    private String identificacion;
    private String password;
    private RolNombre rol;

    public RegisterRequestDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public RolNombre getRol() { return rol; }
    public void setRol(RolNombre rol) { this.rol = rol; }
}