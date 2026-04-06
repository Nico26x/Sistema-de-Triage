package com.triage.backend.service.impl;

import com.triage.backend.service.IUsuarioService;
import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.exception.NotFoundException;
import com.triage.backend.repository.UsuarioRepository;
import com.triage.backend.web.dto.UsuarioDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioServiceImpl implements IUsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    
    @Override
    public List<UsuarioDTO> listar() {
        return usuarioRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public UsuarioDTO detalle(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + id));
        return toDTO(usuario);
    }
    
    @Override
    public void activar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + id));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }
    
    @Override
    public void desactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
    
    @Override
    public List<UsuarioDTO> listarResponsablesActivos() {
        return usuarioRepository.findByActivoTrue().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    private UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getIdentificacion(),
            usuario.isActivo(),
            usuario.getRol()
        );
    }
}

