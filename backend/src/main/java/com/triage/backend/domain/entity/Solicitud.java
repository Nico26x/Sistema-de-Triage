package com.triage.backend.domain.entity;

import com.triage.backend.domain.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "solicitudes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 1000)
    private String descripcion;
    
    @Column(nullable = false)
    private LocalDateTime fechaRegistro;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalOrigen canalOrigen;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estadoActual;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Prioridad prioridad;
    
    @Column(nullable = true, length = 500)
    private String justificacionPrioridad;
    
    @Column(nullable = true)
    private LocalDateTime fechaLimite;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ImpactoAcademico impacto;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TipoSolicitudNombre tipoSolicitud;
    
    @ManyToOne
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;
    
    @ManyToOne
    @JoinColumn(name = "responsable_id", nullable = true)
    private Usuario responsable;
    
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialSolicitud> historial;
}
