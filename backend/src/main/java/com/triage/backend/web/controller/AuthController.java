package com.triage.backend.web.controller;

import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.service.IAuthService;
import com.triage.backend.service.IUsuarioService;
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
    private final IUsuarioService usuarioService;
    
    public AuthController(IAuthService authService, IUsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> register(@RequestBody RegisterRequestDTO req) {
        Usuario usuario = authService.registrar(req);
        UsuarioDTO dto = UsuarioDTO.builder()
            .id(usuario.getId())
            .nombre(usuario.getNombre())
            .email(usuario.getEmail())
            .identificacion(usuario.getIdentificacion())
            .activo(usuario.isActivo())
            .rol(usuario.getRol())
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO req) {
        Usuario usuario = authService.autenticar(req);
        
        // Por ahora, simplemente retornar un token placeholder
        // En el Hito 3 se implementará JWT
        AuthResponseDTO response = AuthResponseDTO.builder()
            .token("placeholder-token-" + usuario.getId() + "-" + System.currentTimeMillis())
            .tipo("Bearer")
            .build();
        
        return ResponseEntity.ok(response);
    }
}
