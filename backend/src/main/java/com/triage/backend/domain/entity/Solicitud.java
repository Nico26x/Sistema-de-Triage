package com.triage.backend.domain.entity;

import com.triage.backend.domain.enums.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origen", nullable = false, length = 30)
    private CanalOrigen canalOrigen;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_actual", nullable = false, length = 30)
    private EstadoSolicitud estadoActual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridad prioridad;

    @Column(name = "justificacion_prioridad", length = 1000)
    private String justificacionPrioridad;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImpactoAcademico impacto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_solicitud", nullable = false, length = 40)
    private TipoSolicitudNombre tipoSolicitud;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_solicitud_solicitante"))
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id",
            foreignKey = @ForeignKey(name = "fk_solicitud_responsable"))
    private Usuario responsable;

    // Snapshot opcional (para auditoría/reportes). NO confiar en lo que envía el front.
    @Column(name = "identificacion_solicitante", length = 50)
    private String identificacionSolicitante;

    @Column(name = "nombre_solicitante", length = 120)
    private String nombreSolicitante;

    @Column(name = "email_solicitante", length = 150)
    private String emailSolicitante;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaHora ASC")
    private List<HistorialSolicitud> historial = new ArrayList<>();

    public Solicitud() {}

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (estadoActual == null) estadoActual = EstadoSolicitud.REGISTRADA;
        if (prioridad == null) prioridad = Prioridad.MEDIA;
        if (tipoSolicitud == null) tipoSolicitud = TipoSolicitudNombre.OTRO;
    }

    public void addHistorial(HistorialSolicitud h) {
        historial.add(h);
        h.setSolicitud(this);
    }

    public void removeHistorial(HistorialSolicitud h) {
        historial.remove(h);
        h.setSolicitud(null);
    }

    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public CanalOrigen getCanalOrigen() { return canalOrigen; }
    public void setCanalOrigen(CanalOrigen canalOrigen) { this.canalOrigen = canalOrigen; }

    public EstadoSolicitud getEstadoActual() { return estadoActual; }
    public void setEstadoActual(EstadoSolicitud estadoActual) { this.estadoActual = estadoActual; }

    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }

    public String getJustificacionPrioridad() { return justificacionPrioridad; }
    public void setJustificacionPrioridad(String justificacionPrioridad) { this.justificacionPrioridad = justificacionPrioridad; }

    public LocalDateTime getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDateTime fechaLimite) { this.fechaLimite = fechaLimite; }

    public ImpactoAcademico getImpacto() { return impacto; }
    public void setImpacto(ImpactoAcademico impacto) { this.impacto = impacto; }

    public TipoSolicitudNombre getTipoSolicitud() { return tipoSolicitud; }
    public void setTipoSolicitud(TipoSolicitudNombre tipoSolicitud) { this.tipoSolicitud = tipoSolicitud; }

    public Usuario getSolicitante() { return solicitante; }
    public void setSolicitante(Usuario solicitante) { this.solicitante = solicitante; }

    public Usuario getResponsable() { return responsable; }
    public void setResponsable(Usuario responsable) { this.responsable = responsable; }

    public String getIdentificacionSolicitante() { return identificacionSolicitante; }
    public void setIdentificacionSolicitante(String identificacionSolicitante) { this.identificacionSolicitante = identificacionSolicitante; }

    public String getNombreSolicitante() { return nombreSolicitante; }
    public void setNombreSolicitante(String nombreSolicitante) { this.nombreSolicitante = nombreSolicitante; }

    public String getEmailSolicitante() { return emailSolicitante; }
    public void setEmailSolicitante(String emailSolicitante) { this.emailSolicitante = emailSolicitante; }

    public List<HistorialSolicitud> getHistorial() { return historial; }
    public void setHistorial(List<HistorialSolicitud> historial) { this.historial = historial; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Solicitud solicitud)) return false;
        return id != null && Objects.equals(id, solicitud.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}