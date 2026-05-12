package com.triage.backend.web.controller;

import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.security.JwtUtil;
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
    private final JwtUtil jwtUtil;
    
    public AuthController(IAuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
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
        
        // Generar JWT real con datos del usuario
        String token = jwtUtil.generarToken(
            usuario.getId(),
            usuario.getEmail(),
            usuario.getRol().name()
        );
        
        AuthResponseDTO response = new AuthResponseDTO(token, "Bearer");
        
        return ResponseEntity.ok(response);
    }
}
