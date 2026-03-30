package com.triage.backend.service;
import com.triage.backend.domain.enums.Prioridad;
import com.triage.backend.domain.enums.ImpactoAcademico;
import com.triage.backend.domain.enums.TipoSolicitudNombre;

import java.time.LocalDateTime;

public interface IPriorizacionService {
    Prioridad calcularPrioridad(ImpactoAcademico impacto, LocalDateTime fechaLimite, TipoSolicitudNombre tipo);
    int calcularPuntuacion(ImpactoAcademico impacto, LocalDateTime fechaLimite, TipoSolicitudNombre tipo);
}