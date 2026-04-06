package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.*;

import java.time.LocalDateTime;

public record SolicitudFilterDTO(
    EstadoSolicitud estado,
    Prioridad prioridad,
    TipoSolicitudNombre tipoSolicitud,
    CanalOrigen canalOrigen,
    Long responsableId,
    LocalDateTime desde,
    LocalDateTime hasta
) {}
