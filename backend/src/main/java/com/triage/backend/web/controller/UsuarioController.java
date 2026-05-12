package com.triage.backend.web.controller;

import com.triage.backend.service.IUsuarioService;
import com.triage.backend.web.dto.UsuarioDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {
    
    private final IUsuarioService usuarioService;
    
    public UsuarioController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    
    /**
     * Lista todos los usuarios.
     * Solo ADMINISTRATIVO y COORDINADOR pueden acceder.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<List<UsuarioDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }
    
    /**
     * Obtiene detalles de un usuario específico.
     * ADMINISTRATIVO y COORDINADOR pueden ver cualquier usuario.
     * ESTUDIANTE solo puede ver su propio usuario.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR') || @usuarioSecurityService.canViewUser(#id, authentication)")
    public ResponseEntity<UsuarioDTO> detalle(@PathVariable("id") Long id) {
        return ResponseEntity.ok(usuarioService.detalle(id));
    }
    
    /**
     * Activa un usuario.
     * Solo COORDINADOR puede hacerlo.
     */
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('COORDINADOR')")
    public ResponseEntity<Void> activar(@PathVariable("id") Long id) {
        usuarioService.activar(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Desactiva un usuario.
     * Solo COORDINADOR puede hacerlo.
     */
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('COORDINADOR')")
    public ResponseEntity<Void> desactivar(@PathVariable("id") Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Lista usuarios responsables activos.
     * ADMINISTRATIVO y COORDINADOR pueden acceder.
     */
    @GetMapping("/responsables")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<List<UsuarioDTO>> listarResponsablesActivos() {
        return ResponseEntity.ok(usuarioService.listarResponsablesActivos());
    }
}

