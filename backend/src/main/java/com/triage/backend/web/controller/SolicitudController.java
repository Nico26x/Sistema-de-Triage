package com.triage.backend.web.controller;

import com.triage.backend.service.ISolicitudService;
import com.triage.backend.web.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) String tipoSolicitud,
            @RequestParam(required = false) String canalOrigen,
            @RequestParam(required = false) Long responsableId,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        
        // Convertir strings a enums (simplificado, se puede mejorar)
        SolicitudFilterDTO filtros = SolicitudFilterDTO.builder()
            .responsableId(responsableId)
            .build();
        
        return ResponseEntity.ok(solicitudService.listar(filtros));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.detalle(id));
    }
    
    @PutMapping("/{id}/clasificar")
    public ResponseEntity<SolicitudResponseDTO> clasificar(
            @PathVariable Long id,
            @RequestBody ClasificarDTO req) {
        SolicitudResponseDTO response = solicitudService.clasificar(id, req);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/asignar")
    public ResponseEntity<SolicitudResponseDTO> asignar(
            @PathVariable Long id,
            @RequestBody AsignarDTO req) {
        SolicitudResponseDTO response = solicitudService.asignarResponsable(id, req);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/estado")
    public ResponseEntity<SolicitudResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestBody CambiarEstadoDTO req) {
        SolicitudResponseDTO response = solicitudService.cambiarEstado(id, req);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/cerrar")
    public ResponseEntity<SolicitudResponseDTO> cerrar(
            @PathVariable Long id,
            @RequestBody CerrarDTO req) {
        SolicitudResponseDTO response = solicitudService.cerrar(id, req);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialEntryDTO>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerHistorial(id));
    }
}
