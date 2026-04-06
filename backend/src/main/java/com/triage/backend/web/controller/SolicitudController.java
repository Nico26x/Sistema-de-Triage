package com.triage.backend.web.controller;

import com.triage.backend.service.ISolicitudService;
import com.triage.backend.web.dto.*;
import com.triage.backend.domain.enums.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    
    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> crear(@RequestBody SolicitudCreateDTO req) {
        SolicitudResponseDTO response = solicitudService.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<SolicitudResponseDTO>> listar(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "prioridad", required = false) String prioridad,
            @RequestParam(value = "tipoSolicitud", required = false) String tipoSolicitud,
            @RequestParam(value = "canalOrigen", required = false) String canalOrigen,
            @RequestParam(value = "responsableId", required = false) Long responsableId,
            @RequestParam(value = "desde", required = false) String desde,
            @RequestParam(value = "hasta", required = false) String hasta) {
        
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
        
        return ResponseEntity.ok(solicitudService.listar(filtros));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> detalle(@PathVariable("id") Long id) {
        return ResponseEntity.ok(solicitudService.detalle(id));
    }
    
    @PutMapping("/{id}/clasificar")
    public ResponseEntity<SolicitudResponseDTO> clasificar(
            @PathVariable("id") Long id,
            @RequestBody ClasificarDTO req) {
        SolicitudResponseDTO response = solicitudService.clasificar(id, req);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/asignar")
    public ResponseEntity<SolicitudResponseDTO> asignar(
            @PathVariable("id") Long id,
            @RequestBody AsignarDTO req) {
        SolicitudResponseDTO response = solicitudService.asignarResponsable(id, req);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/estado")
    public ResponseEntity<SolicitudResponseDTO> cambiarEstado(
            @PathVariable("id") Long id,
            @RequestBody CambiarEstadoDTO req) {
        SolicitudResponseDTO response = solicitudService.cambiarEstado(id, req);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/cerrar")
    public ResponseEntity<SolicitudResponseDTO> cerrar(
            @PathVariable("id") Long id,
            @RequestBody CerrarDTO req) {
        SolicitudResponseDTO response = solicitudService.cerrar(id, req);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialEntryDTO>> historial(@PathVariable("id") Long id) {
        return ResponseEntity.ok(solicitudService.obtenerHistorial(id));
    }
}
