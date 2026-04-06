package com.triage.backend.web.dto;

import com.triage.backend.domain.enums.CanalOrigen;
import com.triage.backend.domain.enums.ImpactoAcademico;
import com.triage.backend.domain.enums.TipoSolicitudNombre;

import java.time.LocalDateTime;

public record SolicitudCreateDTO(
    String descripcion,
    CanalOrigen canal,
    Long solicitanteId,
    ImpactoAcademico impacto,
    LocalDateTime fechaLimite,
    TipoSolicitudNombre tipo
) {}
