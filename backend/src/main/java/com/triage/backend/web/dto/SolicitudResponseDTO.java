package com.triage.backend.web.dto;

import java.time.LocalDateTime;

import com.triage.backend.domain.enums.CanalOrigen;
import com.triage.backend.domain.enums.EstadoSolicitud;
import com.triage.backend.domain.enums.ImpactoAcademico;
import com.triage.backend.domain.enums.Prioridad;
import com.triage.backend.domain.enums.TipoSolicitudNombre;

public record SolicitudResponseDTO(
    Long id,
    String descripcion,
    LocalDateTime fechaRegistro,
    EstadoSolicitud estado,
    Prioridad prioridad,
    String justificacionPrioridad,
    CanalOrigen canalOrigen,
    TipoSolicitudNombre tipoSolicitud,
    String solicitante,
    String responsable,
    LocalDateTime fechaLimite,
    ImpactoAcademico impacto
) {}
