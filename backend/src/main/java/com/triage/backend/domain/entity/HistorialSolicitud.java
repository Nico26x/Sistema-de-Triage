package com.triage.backend.domain.entity;

import com.triage.backend.domain.enums.AccionHistorial;
import com.triage.backend.domain.enums.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_solicitudes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialSolicitud implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;
    
    @Column(nullable = false)
    private LocalDateTime fechaHora;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccionHistorial accion;
    
    @Column(nullable = true, length = 500)
    private String observacion;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private EstadoSolicitud estadoAnterior;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private EstadoSolicitud estadoNuevo;
    
    @ManyToOne
    @JoinColumn(name = "actor_id", nullable = true)
    private Usuario actor;
}
