package com.triage.backend.security;

import com.triage.backend.domain.enums.RolNombre;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Componente de seguridad para validar acceso a usuarios basado en roles.
 * Usado en @PreAuthorize para autorización a nivel de método.
 */
@Component("usuarioSecurityService")
public class UsuarioSecurityService {
    
    /**
     * Valida si el usuario autenticado puede ver un usuario específico.
     * COORDINADOR y ADMINISTRATIVO: pueden ver cualquier usuario.
     * ESTUDIANTE: solo puede verse a sí mismo.
     */
    public boolean canViewUser(Long userId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long usuarioId = userDetails.getUsuarioId();
        RolNombre rol = userDetails.getRol();
        
        // COORDINADOR y ADMINISTRATIVO pueden ver cualquier usuario
        if (rol == RolNombre.COORDINADOR || rol == RolNombre.ADMINISTRATIVO) {
            return true;
        }
        
        // ESTUDIANTE solo puede verse a sí mismo
        if (rol == RolNombre.ESTUDIANTE) {
            return usuarioId.equals(userId);
        }
        
        return false;
    }
}
