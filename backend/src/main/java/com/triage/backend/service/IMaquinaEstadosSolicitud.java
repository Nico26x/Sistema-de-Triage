package com.triage.backend.service;

import com.triage.backend.domain.enums.EstadoSolicitud;

public interface IMaquinaEstadosSolicitud {
    void validarTransicion(EstadoSolicitud estadoActual, EstadoSolicitud nuevoEstado);
    boolean esTransicionValida(EstadoSolicitud estadoActual, EstadoSolicitud nuevoEstado);
}