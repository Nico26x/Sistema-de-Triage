package com.triage.backend.web.controller;

import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.service.IAuthService;
import com.triage.backend.web.dto.AuthRequestDTO;
import com.triage.backend.web.dto.AuthResponseDTO;
import com.triage.backend.web.dto.RegisterRequestDTO;
import com.triage.backend.web.dto.UsuarioDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final IAuthService authService;
    
    public AuthController(IAuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> register(@RequestBody RegisterRequestDTO req) {
        Usuario usuario = authService.registrar(req);
        UsuarioDTO dto = new UsuarioDTO(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getIdentificacion(),
            usuario.isActivo(),
            usuario.getRol()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO req) {
        Usuario usuario = authService.autenticar(req);
        
        // Por ahora, simplemente retornar un token placeholder
        // En el Hito 3 se implementará JWT
        AuthResponseDTO response = new AuthResponseDTO(
            "placeholder-token-" + usuario.getId() + "-" + System.currentTimeMillis(),
            "Bearer"
        );
        
        return ResponseEntity.ok(response);
    }
}
