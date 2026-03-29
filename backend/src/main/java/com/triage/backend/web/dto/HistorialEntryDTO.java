package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.AccionHistorial;
import com.triage.backend.domain.enums.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEntryDTO {
    private LocalDateTime fechaHora;
    private AccionHistorial accion;
    private String observacion;
    private EstadoSolicitud estadoAnterior;
    private EstadoSolicitud estadoNuevo;
    private Long actorId;
}
