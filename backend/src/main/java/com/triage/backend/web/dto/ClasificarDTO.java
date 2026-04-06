package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.ImpactoAcademico;
import com.triage.backend.domain.enums.TipoSolicitudNombre;

import java.time.LocalDateTime;

public record ClasificarDTO(
    TipoSolicitudNombre tipoSolicitud,
    ImpactoAcademico impacto,
    LocalDateTime fechaLimite,
    String observacion
) {}
