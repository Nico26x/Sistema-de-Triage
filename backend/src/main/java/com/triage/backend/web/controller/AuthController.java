package com.triage.backend.web.controller;

import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.service.AuthService;
import com.triage.backend.web.dto.AuthRequestDTO;
import com.triage.backend.web.dto.AuthResponseDTO;
import com.triage.backend.web.dto.RegisterRequestDTO;
import com.triage.backend.web.dto.UsuarioDTO;
import com.triage.backend.web.mapper.UsuarioMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> register(@RequestBody RegisterRequestDTO req) {
        Usuario u = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioMapper.toDto(u));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO req) {
        return ResponseEntity.ok(authService.login(req));
    }
}