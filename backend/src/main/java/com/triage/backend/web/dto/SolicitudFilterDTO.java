package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudFilterDTO {
    private EstadoSolicitud estado;
    private Prioridad prioridad;
    private TipoSolicitudNombre tipoSolicitud;
    private CanalOrigen canalOrigen;
    private Long responsableId;
    private LocalDateTime desde;
    private LocalDateTime hasta;
}
