package com.triage.backend.service;

import com.triage.backend.domain.enums.Prioridad;

public class ResultadoPriorizacion {

    private final Prioridad prioridad;
    private final String justificacion;

    public ResultadoPriorizacion(Prioridad prioridad, String justificacion) {
        this.prioridad = prioridad;
        this.justificacion = justificacion;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public String getJustificacion() {
        return justificacion;
    }
}