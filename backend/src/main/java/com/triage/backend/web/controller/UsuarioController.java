package com.triage.backend.web.controller;

import com.triage.backend.service.IUsuarioService;
import com.triage.backend.web.dto.UsuarioDTO;
import org.springframework.http.ResponseEntity;
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
    
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.detalle(id));
    }
    
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        usuarioService.activar(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/responsables")
    public ResponseEntity<List<UsuarioDTO>> listarResponsablesActivos() {
        return ResponseEntity.ok(usuarioService.listarResponsablesActivos());
    }
}

