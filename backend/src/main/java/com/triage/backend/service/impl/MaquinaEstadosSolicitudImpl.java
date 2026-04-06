package com.triage.backend.service.impl;

import com.triage.backend.service.IMaquinaEstadosSolicitud;
import com.triage.backend.domain.enums.EstadoSolicitud;
import com.triage.backend.exception.BusinessRuleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MaquinaEstadosSolicitudImpl implements IMaquinaEstadosSolicitud {
    
    @Override
    public void validarTransicion(EstadoSolicitud estadoActual, EstadoSolicitud nuevoEstado) {
        if (!esTransicionValida(estadoActual, nuevoEstado)) {
            throw new BusinessRuleException(
                String.format("Transición inválida de %s a %s", estadoActual, nuevoEstado)
            );
        }
    }
    
    @Override
    public boolean esTransicionValida(EstadoSolicitud estadoActual, EstadoSolicitud nuevoEstado) {
        // Máquina de estados lineal
        return switch (estadoActual) {
            case REGISTRADA -> nuevoEstado == EstadoSolicitud.CLASIFICADA;
            case CLASIFICADA -> nuevoEstado == EstadoSolicitud.EN_ATENCION;
            case EN_ATENCION -> nuevoEstado == EstadoSolicitud.ATENDIDA;
            case ATENDIDA -> nuevoEstado == EstadoSolicitud.CERRADA;
            case CERRADA -> false;
            default -> false;
        };
    }
}

