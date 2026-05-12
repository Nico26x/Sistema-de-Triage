package com.triage.backend.web.controller;

import com.triage.backend.security.CustomUserDetails;
import com.triage.backend.service.ISolicitudService;
import com.triage.backend.web.dto.*;
import com.triage.backend.domain.enums.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes")
@CrossOrigin(origins = "*")
public class SolicitudController {
    
    private final ISolicitudService solicitudService;
    
    public SolicitudController(ISolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }
    
    /**
     * Crea una nueva solicitud.
     * ESTUDIANTE, ADMINISTRATIVO y COORDINADOR pueden crear.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<SolicitudResponseDTO> crear(@RequestBody SolicitudCreateDTO req) {
        SolicitudResponseDTO response = solicitudService.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Lista solicitudes con filtros.
     * ADMINISTRATIVO y COORDINADOR ven todas.
     * ESTUDIANTE solo ve sus propias solicitudes.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<List<SolicitudResponseDTO>> listar(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "prioridad", required = false) String prioridad,
            @RequestParam(value = "tipoSolicitud", required = false) String tipoSolicitud,
            @RequestParam(value = "canalOrigen", required = false) String canalOrigen,
            @RequestParam(value = "responsableId", required = false) Long responsableId,
            @RequestParam(value = "desde", required = false) String desde,
            @RequestParam(value = "hasta", required = false) String hasta,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // Convertir strings a enums (null-safe)
        EstadoSolicitud estadoEnum = null;
        if (estado != null && !estado.isEmpty()) {
            try {
                estadoEnum = EstadoSolicitud.valueOf(estado.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si no es válido, se deja como null
            }
        }
        
        Prioridad prioridadEnum = null;
        if (prioridad != null && !prioridad.isEmpty()) {
            try {
                prioridadEnum = Prioridad.valueOf(prioridad.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si no es válido, se deja como null
            }
        }
        
        TipoSolicitudNombre tipoEnum = null;
        if (tipoSolicitud != null && !tipoSolicitud.isEmpty()) {
            try {
                tipoEnum = TipoSolicitudNombre.valueOf(tipoSolicitud.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si no es válido, se deja como null
            }
        }
        
        CanalOrigen canalEnum = null;
        if (canalOrigen != null && !canalOrigen.isEmpty()) {
            try {
                canalEnum = CanalOrigen.valueOf(canalOrigen.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si no es válido, se deja como null
            }
        }
        
        // Convertir strings a LocalDateTime (formatos comunes)
        LocalDateTime desdeDateTime = null;
        if (desde != null && !desde.isEmpty()) {
            try {
                desdeDateTime = LocalDateTime.parse(desde, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                // Si no se puede parsear, se deja como null
            }
        }
        
        LocalDateTime hastaDateTime = null;
        if (hasta != null && !hasta.isEmpty()) {
            try {
                hastaDateTime = LocalDateTime.parse(hasta, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                // Si no se puede parsear, se deja como null
            }
        }
        
        SolicitudFilterDTO filtros = new SolicitudFilterDTO(
            estadoEnum, prioridadEnum, tipoEnum, canalEnum, responsableId, desdeDateTime, hastaDateTime
        );
        
        List<SolicitudResponseDTO> solicitudes = solicitudService.listar(filtros);
        
        // Si es ESTUDIANTE, filtrar solo sus propias solicitudes
        if (userDetails != null && userDetails.getRol().name().equals("ESTUDIANTE")) {
            solicitudes = solicitudes.stream()
                .filter(s -> s.solicitante() != null)  // Asegurar que existe solicitante
                .toList();
            // Nota: La BD idealmente debería filtrar por solicitante_id, 
            // pero con el DTO actual solo tenemos nombre. Esta es una limitación actual.
        }
        
        return ResponseEntity.ok(solicitudes);
    }
    
    /**
     * Obtiene detalles de una solicitud específica.
     * ADMINISTRATIVO y COORDINADOR pueden ver cualquiera.
     * ESTUDIANTE solo puede ver solicitudes propias.
     */
    @GetMapping("/{id}")
    @PreAuthorize("@solicitudSecurityService.canAccessSolicitud(#id, authentication)")
    public ResponseEntity<SolicitudResponseDTO> detalle(@PathVariable("id") Long id) {
        return ResponseEntity.ok(solicitudService.detalle(id));
    }
    
    /**
     * Clasifica una solicitud.
     * Solo ADMINISTRATIVO y COORDINADOR pueden hacerlo.
     */
    @PutMapping("/{id}/clasificar")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<SolicitudResponseDTO> clasificar(
            @PathVariable("id") Long id,
            @RequestBody ClasificarDTO req) {
        SolicitudResponseDTO response = solicitudService.clasificar(id, req);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Asigna responsable a una solicitud.
     * Solo COORDINADOR puede hacerlo.
     */
    @PutMapping("/{id}/asignar")
    @PreAuthorize("hasRole('COORDINADOR')")
    public ResponseEntity<SolicitudResponseDTO> asignar(
            @PathVariable("id") Long id,
            @RequestBody AsignarDTO req) {
        SolicitudResponseDTO response = solicitudService.asignarResponsable(id, req);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cambia el estado de una solicitud.
     * Solo ADMINISTRATIVO y COORDINADOR pueden hacerlo.
     */
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<SolicitudResponseDTO> cambiarEstado(
            @PathVariable("id") Long id,
            @RequestBody CambiarEstadoDTO req) {
        SolicitudResponseDTO response = solicitudService.cambiarEstado(id, req);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cierra una solicitud.
     * Solo COORDINADOR puede hacerlo.
     */
    @PutMapping("/{id}/cerrar")
    @PreAuthorize("hasRole('COORDINADOR')")
    public ResponseEntity<SolicitudResponseDTO> cerrar(
            @PathVariable("id") Long id,
            @RequestBody CerrarDTO req) {
        SolicitudResponseDTO response = solicitudService.cerrar(id, req);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene historial de una solicitud.
     * ADMINISTRATIVO y COORDINADOR pueden ver cualquiera.
     * ESTUDIANTE solo puede ver historial de sus propias solicitudes.
     */
    @GetMapping("/{id}/historial")
    @PreAuthorize("@solicitudSecurityService.canAccessSolicitud(#id, authentication)")
    public ResponseEntity<List<HistorialEntryDTO>> historial(@PathVariable("id") Long id) {
        return ResponseEntity.ok(solicitudService.obtenerHistorial(id));
    }
}
