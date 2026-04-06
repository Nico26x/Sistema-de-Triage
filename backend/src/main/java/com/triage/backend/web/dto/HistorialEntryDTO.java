package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.AccionHistorial;
import com.triage.backend.domain.enums.EstadoSolicitud;

import java.time.LocalDateTime;

public record HistorialEntryDTO(
    LocalDateTime fechaHora,
    AccionHistorial accion,
    String observacion,
    EstadoSolicitud estadoAnterior,
    EstadoSolicitud estadoNuevo,
    Long actorId
) {}
