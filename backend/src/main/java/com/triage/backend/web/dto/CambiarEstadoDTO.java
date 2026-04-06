package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.EstadoSolicitud;

public record CambiarEstadoDTO(
    EstadoSolicitud nuevoEstado,
    String observacion
) {}
