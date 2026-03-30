package com.triage.backend.service;
import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.web.dto.AuthRequestDTO;
import com.triage.backend.web.dto.RegisterRequestDTO;

public interface IAuthService {
    Usuario registrar(RegisterRequestDTO req);
    Usuario autenticar(AuthRequestDTO req);
}