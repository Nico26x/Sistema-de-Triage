package com.triage.backend.service.rules;

import com.triage.backend.domain.entity.Solicitud;

public interface ReglaPriorizacion {

    /**
     * Retorna puntos para la solicitud (0..n)
     */
    int evaluar(Solicitud solicitud);

    /**
     * Retorna texto explicativo para la justificación.
     * Ej: "Impacto=ALTO(+30)"
     */
    String explicar(Solicitud solicitud);
}