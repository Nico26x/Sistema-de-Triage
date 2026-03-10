package com.triage.backend.service;

import com.triage.backend.domain.entity.HistorialSolicitud;
import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.AccionHistorial;
import com.triage.backend.domain.enums.EstadoSolicitud;
import com.triage.backend.exception.BusinessRuleException;
import com.triage.backend.repository.HistorialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistorialService {

    private final HistorialRepository historialRepository;

    public HistorialService(HistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @Transactional
    public HistorialSolicitud registrarEvento(
            Solicitud solicitud,
            Usuario actor,
            AccionHistorial accion,
            String observacion,
            EstadoSolicitud estadoAnterior,
            EstadoSolicitud estadoNuevo
    ) {
        if (solicitud == null) throw new BusinessRuleException("La solicitud es obligatoria.");
        if (actor == null) throw new BusinessRuleException("El actor (usuario) es obligatorio.");
        if (accion == null) throw new BusinessRuleException("La acción del historial es obligatoria.");
        if (estadoNuevo == null) throw new BusinessRuleException("El estado nuevo es obligatorio.");

        HistorialSolicitud h = new HistorialSolicitud();
        h.setSolicitud(solicitud);
        h.setActor(actor);
        h.setAccion(accion);
        h.setObservacion(observacion);
        h.setEstadoAnterior(estadoAnterior);
        h.setEstadoNuevo(estadoNuevo);

        // IMPORTANTE: como Solicitud tiene cascade ALL hacia historial,
        // podríamos agregarlo con solicitud.addHistorial(h) y guardar solicitud.
        // Aquí lo guardamos directo para mantener HistorialService independiente.
        return historialRepository.save(h);
    }

    @Transactional(readOnly = true)
    public List<HistorialSolicitud> obtenerHistorial(Long solicitudId) {
        return historialRepository.findBySolicitud_IdOrderByFechaHoraAsc(solicitudId);
    }
}