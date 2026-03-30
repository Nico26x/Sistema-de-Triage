package com.triage.backend.service.impl;


import com.triage.backend.service.ISolicitudService;
import com.triage.backend.service.IHistorialService;
import com.triage.backend.service.IPriorizacionService;
import com.triage.backend.service.IMaquinaEstadosSolicitud;
import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.AccionHistorial;
import com.triage.backend.domain.enums.EstadoSolicitud;
import com.triage.backend.exception.BusinessRuleException;
import com.triage.backend.exception.NotFoundException;
import com.triage.backend.repository.SolicitudRepository;
import com.triage.backend.repository.UsuarioRepository;
import com.triage.backend.web.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SolicitudServiceImpl implements ISolicitudService {
    
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final IHistorialService historialService;
    private final IPriorizacionService priorizacionService;
    private final IMaquinaEstadosSolicitud maquinaEstados;
    
    public SolicitudServiceImpl(SolicitudRepository solicitudRepository, 
                               UsuarioRepository usuarioRepository,
                               IHistorialService historialService,
                               IPriorizacionService priorizacionService,
                               IMaquinaEstadosSolicitud maquinaEstados) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialService = historialService;
        this.priorizacionService = priorizacionService;
        this.maquinaEstados = maquinaEstados;
    }
    
    @Override
    public SolicitudResponseDTO crear(SolicitudCreateDTO dto) {
        Usuario solicitante = usuarioRepository.findById(dto.getSolicitanteId())
            .orElseThrow(() -> new NotFoundException("Solicitante no encontrado"));
        
        if (!solicitante.isActivo()) {
            throw new BusinessRuleException("El solicitante debe estar activo");
        }
        
        Solicitud solicitud = Solicitud.builder()
            .descripcion(dto.getDescripcion())
            .canalOrigen(dto.getCanal())
            .estadoActual(EstadoSolicitud.REGISTRADA)
            .tipoSolicitud(dto.getTipo())
            .impacto(dto.getImpacto())
            .fechaLimite(dto.getFechaLimite())
            .solicitante(solicitante)
            .fechaRegistro(LocalDateTime.now())
            .prioridad(priorizacionService.calcularPrioridad(dto.getImpacto(), 
                                                             dto.getFechaLimite(), 
                                                             dto.getTipo()))
            .build();
        
        Solicitud guardada = solicitudRepository.save(solicitud);
        
        historialService.registrarEvento(guardada, solicitante, AccionHistorial.REGISTRO, 
                                        "Solicitud registrada", null, EstadoSolicitud.REGISTRADA);
        
        return toResponseDTO(guardada);
    }
    
    @Override
    public List<SolicitudResponseDTO> listar(SolicitudFilterDTO filtro) {
        List<Solicitud> solicitudes = solicitudRepository.buscarPorFiltros(
            filtro.getEstado(),
            filtro.getPrioridad(),
            filtro.getTipoSolicitud(),
            filtro.getCanalOrigen(),
            filtro.getResponsableId(),
            filtro.getDesde(),
            filtro.getHasta()
        );
        
        return solicitudes.stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public SolicitudResponseDTO detalle(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));
        return toResponseDTO(solicitud);
    }
    
    @Override
    public SolicitudResponseDTO clasificar(Long id, ClasificarDTO dto) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));
        
        if (solicitud.getEstadoActual() != EstadoSolicitud.REGISTRADA) {
            throw new BusinessRuleException("Solo se pueden clasificar solicitudes en estado REGISTRADA");
        }
        
        // Validar que todos los campos requeridos estén presentes
        if (dto.getTipoSolicitud() == null) {
            throw new BusinessRuleException("El tipo de solicitud es requerido");
        }
        if (dto.getImpacto() == null) {
            throw new BusinessRuleException("El impacto es requerido");
        }
        if (dto.getFechaLimite() == null) {
            throw new BusinessRuleException("La fecha límite es requerida");
        }
        
        solicitud.setTipoSolicitud(dto.getTipoSolicitud());
        solicitud.setImpacto(dto.getImpacto());
        solicitud.setFechaLimite(dto.getFechaLimite());
        solicitud.setPrioridad(priorizacionService.calcularPrioridad(dto.getImpacto(), 
                                                                      dto.getFechaLimite(), 
                                                                      dto.getTipoSolicitud()));
        
        maquinaEstados.validarTransicion(EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA);
        solicitud.setEstadoActual(EstadoSolicitud.CLASIFICADA);
        
        Solicitud guardada = solicitudRepository.save(solicitud);
        
        historialService.registrarEvento(guardada, null, AccionHistorial.CLASIFICACION, 
                                        dto.getObservacion(), EstadoSolicitud.REGISTRADA, 
                                        EstadoSolicitud.CLASIFICADA);
        
        return toResponseDTO(guardada);
    }
    
    @Override
    public SolicitudResponseDTO asignarResponsable(Long id, AsignarDTO dto) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));
        
        Usuario responsable = usuarioRepository.findById(dto.getResponsableId())
            .orElseThrow(() -> new NotFoundException("Responsable no encontrado"));
        
        if (!responsable.isActivo()) {
            throw new BusinessRuleException("El responsable debe estar activo");
        }
        
        solicitud.setResponsable(responsable);
        Solicitud guardada = solicitudRepository.save(solicitud);
        
        historialService.registrarEvento(guardada, responsable, AccionHistorial.ASIGNACION, 
                                        "Asignado a: " + responsable.getNombre(), 
                                        solicitud.getEstadoActual(), solicitud.getEstadoActual());
        
        return toResponseDTO(guardada);
    }
    
    @Override
    public SolicitudResponseDTO cambiarEstado(Long id, CambiarEstadoDTO dto) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));
        
        if (solicitud.getEstadoActual() == EstadoSolicitud.CERRADA) {
            throw new BusinessRuleException("No se puede cambiar el estado de una solicitud cerrada");
        }
        
        maquinaEstados.validarTransicion(solicitud.getEstadoActual(), dto.getNuevoEstado());
        
        EstadoSolicitud estadoAnterior = solicitud.getEstadoActual();
        solicitud.setEstadoActual(dto.getNuevoEstado());
        
        Solicitud guardada = solicitudRepository.save(solicitud);
        
        historialService.registrarEvento(guardada, null, AccionHistorial.CAMBIO_ESTADO, 
                                        dto.getObservacion(), estadoAnterior, dto.getNuevoEstado());
        
        return toResponseDTO(guardada);
    }
    
    @Override
    public SolicitudResponseDTO cerrar(Long id, CerrarDTO dto) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));
        
        if (solicitud.getEstadoActual() == EstadoSolicitud.CERRADA) {
            throw new BusinessRuleException("La solicitud ya está cerrada");
        }
        
        EstadoSolicitud estadoAnterior = solicitud.getEstadoActual();
        solicitud.setEstadoActual(EstadoSolicitud.CERRADA);
        
        Solicitud guardada = solicitudRepository.save(solicitud);
        
        historialService.registrarEvento(guardada, null, AccionHistorial.CIERRE, 
                                        dto.getObservacion(), estadoAnterior, 
                                        EstadoSolicitud.CERRADA);
        
        return toResponseDTO(guardada);
    }
    
    @Override
    public List<HistorialEntryDTO> obtenerHistorial(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));
        
        return historialService.listarPorSolicitud(id);
    }
    
    private SolicitudResponseDTO toResponseDTO(Solicitud solicitud) {
        return SolicitudResponseDTO.builder()
            .id(solicitud.getId())
            .estado(solicitud.getEstadoActual())
            .prioridad(solicitud.getPrioridad())
            .justificacionPrioridad(solicitud.getJustificacionPrioridad())
            .descripcion(solicitud.getDescripcion())
            .tipoSolicitud(solicitud.getTipoSolicitud())
            .canalOrigen(solicitud.getCanalOrigen())
            .impacto(solicitud.getImpacto())
            .fechaLimite(solicitud.getFechaLimite())
            .solicitante(solicitud.getSolicitante().getNombre())
            .responsable(solicitud.getResponsable() != null ? solicitud.getResponsable().getNombre() : null)
            .fechaRegistro(solicitud.getFechaRegistro())
            .build();
    }
}
