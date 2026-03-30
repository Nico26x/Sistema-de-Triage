package com.triage.backend.service.impl;

import com.triage.backend.service.IAuthService;
import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.RolNombre;
import com.triage.backend.exception.BusinessRuleException;
import com.triage.backend.exception.NotFoundException;
import com.triage.backend.repository.UsuarioRepository;
import com.triage.backend.web.dto.AuthRequestDTO;
import com.triage.backend.web.dto.RegisterRequestDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements IAuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    public AuthServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public Usuario registrar(RegisterRequestDTO req) {
        // Validar que el email no exista
        if (usuarioRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new BusinessRuleException("El email ya está registrado");
        }
        
        // Validar que la identificación no exista
        if (usuarioRepository.findByIdentificacion(req.getIdentificacion()).isPresent()) {
            throw new BusinessRuleException("La identificación ya está registrada");
        }
        
        Usuario usuario = Usuario.builder()
            .nombre(req.getNombre())
            .email(req.getEmail())
            .identificacion(req.getIdentificacion())
            .passwordHash(passwordEncoder.encode(req.getPassword()))
            .activo(true)
            .rol(req.getRol())
            .build();
        
        return usuarioRepository.save(usuario);
    }
    
    @Override
    public Usuario autenticar(AuthRequestDTO req) {
        Usuario usuario = usuarioRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        
        if (!passwordEncoder.matches(req.getPassword(), usuario.getPasswordHash())) {
            throw new BusinessRuleException("Contraseña incorrecta");
        }
        
        if (!usuario.isActivo()) {
            throw new BusinessRuleException("El usuario está inactivo");
        }
        
        return usuario;
    }
}
