package com.triage.backend.web.dto;

import java.time.LocalDateTime;

import com.triage.backend.domain.enums.CanalOrigen;
import com.triage.backend.domain.enums.EstadoSolicitud;
import com.triage.backend.domain.enums.ImpactoAcademico;
import com.triage.backend.domain.enums.Prioridad;
import com.triage.backend.domain.enums.TipoSolicitudNombre;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudResponseDTO {
    private Long id;
    private String descripcion;
    private LocalDateTime fechaRegistro;
    private EstadoSolicitud estado;
    private Prioridad prioridad;
    private String justificacionPrioridad;
    private CanalOrigen canalOrigen;
    private TipoSolicitudNombre tipoSolicitud;
    private String solicitante;
    private String responsable;
    private LocalDateTime fechaLimite;
    private ImpactoAcademico impacto;
}
