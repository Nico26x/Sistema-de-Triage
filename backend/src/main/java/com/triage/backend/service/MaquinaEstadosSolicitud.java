package com.triage.backend.service;

import com.triage.backend.domain.enums.EstadoSolicitud;
import com.triage.backend.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class MaquinaEstadosSolicitud {

    private final Map<EstadoSolicitud, Set<EstadoSolicitud>> transicionesPermitidas;

    public MaquinaEstadosSolicitud() {
        // Mapa de transiciones válidas según el ciclo definido:
        // REGISTRADA → CLASIFICADA → EN_ATENCION → ATENDIDA → CERRADA
        transicionesPermitidas = new EnumMap<>(EstadoSolicitud.class);

        transicionesPermitidas.put(EstadoSolicitud.REGISTRADA,
                EnumSet.of(EstadoSolicitud.CLASIFICADA));

        transicionesPermitidas.put(EstadoSolicitud.CLASIFICADA,
                EnumSet.of(EstadoSolicitud.EN_ATENCION));

        transicionesPermitidas.put(EstadoSolicitud.EN_ATENCION,
                EnumSet.of(EstadoSolicitud.ATENDIDA));

        transicionesPermitidas.put(EstadoSolicitud.ATENDIDA,
                EnumSet.of(EstadoSolicitud.CERRADA));

        transicionesPermitidas.put(EstadoSolicitud.CERRADA,
                EnumSet.noneOf(EstadoSolicitud.class)); // estado final
    }

    /**
     * Valida que la transición de estado sea permitida.
     * Si no lo es, lanza BusinessRuleException (409 Conflict).
     */
    public void validarTransicion(EstadoSolicitud actual, EstadoSolicitud nuevo) {
        if (actual == null || nuevo == null) {
            throw new BusinessRuleException("Estado actual y nuevo estado son obligatorios.");
        }

        if (actual == EstadoSolicitud.CERRADA) {
            throw new BusinessRuleException("La solicitud está CERRADA y no puede modificarse.");
        }

        Set<EstadoSolicitud> permitidos = transicionesPermitidas.getOrDefault(actual, EnumSet.noneOf(EstadoSolicitud.class));

        if (!permitidos.contains(nuevo)) {
            throw new BusinessRuleException("Transición inválida de " + actual + " a " + nuevo + ".");
        }
    }

    /**
     * Útil si quieres mostrar desde API qué estados son válidos desde uno dado.
     */
    public Set<EstadoSolicitud> siguientesValidos(EstadoSolicitud actual) {
        return transicionesPermitidas.getOrDefault(actual, EnumSet.noneOf(EstadoSolicitud.class));
    }
}