package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.ImpactoAcademico;
import com.triage.backend.domain.enums.TipoSolicitudNombre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasificarDTO {
    private TipoSolicitudNombre tipoSolicitud;
    private ImpactoAcademico impacto;
    private LocalDateTime fechaLimite;
    private String observacion;
}
