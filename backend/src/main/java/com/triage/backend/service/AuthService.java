package com.triage.backend.service;

import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.RolNombre;
import com.triage.backend.exception.BusinessRuleException;
import com.triage.backend.exception.NotFoundException;
import com.triage.backend.repository.UsuarioRepository;
import com.triage.backend.web.dto.AuthRequestDTO;
import com.triage.backend.web.dto.AuthResponseDTO;
import com.triage.backend.web.dto.RegisterRequestDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario register(RegisterRequestDTO req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new BusinessRuleException("El email es obligatorio.");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new BusinessRuleException("La contraseña es obligatoria.");
        }
        if (req.getNombre() == null || req.getNombre().isBlank()) {
            throw new BusinessRuleException("El nombre es obligatorio.");
        }
        if (req.getIdentificacion() == null || req.getIdentificacion().isBlank()) {
            throw new BusinessRuleException("La identificación es obligatoria.");
        }

        if (usuarioRepository.existsByEmail(req.getEmail())) {
            throw new BusinessRuleException("Ya existe un usuario con ese email.");
        }
        if (usuarioRepository.existsByIdentificacion(req.getIdentificacion())) {
            throw new BusinessRuleException("Ya existe un usuario con esa identificación.");
        }

        RolNombre rol = (req.getRol() == null) ? RolNombre.ESTUDIANTE : req.getRol();

        Usuario u = new Usuario();
        u.setNombre(req.getNombre());
        u.setEmail(req.getEmail());
        u.setIdentificacion(req.getIdentificacion());
        u.setRol(rol);
        u.setActivo(true);

        // BCrypt
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        return usuarioRepository.save(u);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(AuthRequestDTO req) {
        Usuario u = usuarioRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new NotFoundException("Credenciales inválidas."));

        if (!u.isActivo()) {
            throw new BusinessRuleException("Usuario inactivo.");
        }

        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new NotFoundException("Credenciales inválidas.");
        }

        // Token PROVISIONAL (NO JWT): solo para pruebas.
        // Puedes ignorarlo y usar userId como X-Actor-Id.
        String provisionalToken = "PROV-" + UUID.randomUUID();

        return new AuthResponseDTO(
                provisionalToken,
                "Provisional",
                u.getId(),
                u.getEmail(),
                u.getRol()
        );
    }
}