package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambiarEstadoDTO {
    private EstadoSolicitud nuevoEstado;
    private String observacion;
}
