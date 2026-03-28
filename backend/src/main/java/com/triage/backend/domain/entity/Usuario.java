package com.triage.backend.domain.entity;

import com.triage.backend.domain.enums.RolNombre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false, unique = true)
    private String identificacion;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Column(nullable = false)
    private boolean activo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolNombre rol;
}
