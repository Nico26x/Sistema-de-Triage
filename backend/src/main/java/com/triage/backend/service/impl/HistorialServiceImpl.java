package com.triage.backend.service.impl;

import com.triage.backend.service.IHistorialService;
import com.triage.backend.domain.entity.HistorialSolicitud;
import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.AccionHistorial;
import com.triage.backend.domain.enums.EstadoSolicitud;
import com.triage.backend.repository.HistorialRepository;
import com.triage.backend.web.dto.HistorialEntryDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HistorialServiceImpl implements IHistorialService {
    
    private final HistorialRepository historialRepository;
    
    public HistorialServiceImpl(HistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }
    
    @Override
    public void registrarEvento(Solicitud solicitud, Usuario actor, AccionHistorial accion, 
                                 String observacion, EstadoSolicitud estadoAnterior, 
                                 EstadoSolicitud estadoNuevo) {
        HistorialSolicitud historial = HistorialSolicitud.builder()
            .solicitud(solicitud)
            .actor(actor)
            .accion(accion)
            .observacion(observacion)
            .estadoAnterior(estadoAnterior)
            .estadoNuevo(estadoNuevo)
            .fechaHora(LocalDateTime.now())
            .build();
        
        historialRepository.save(historial);
    }
    
    @Override
    public List<HistorialEntryDTO> listarPorSolicitud(Long solicitudId) {
        return historialRepository.findBySolicitudIdOrderByFechaHoraAsc(solicitudId)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    private HistorialEntryDTO toDTO(HistorialSolicitud historial) {
        return HistorialEntryDTO.builder()
            .fechaHora(historial.getFechaHora())
            .accion(historial.getAccion())
            .observacion(historial.getObservacion())
            .estadoAnterior(historial.getEstadoAnterior())
            .estadoNuevo(historial.getEstadoNuevo())
            .actorId(historial.getActor() != null ? historial.getActor().getId() : null)
            .build();
    }
}
