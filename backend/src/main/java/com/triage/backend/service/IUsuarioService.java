package com.triage.backend.service;

import com.triage.backend.web.dto.UsuarioDTO;

import java.util.List;

public interface IUsuarioService {
    List<UsuarioDTO> listar();
    UsuarioDTO detalle(Long id);
    void activar(Long id);
    void desactivar(Long id);
    List<UsuarioDTO> listarResponsablesActivos();
}
