package com.triage.backend.security;

import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.enums.RolNombre;
import com.triage.backend.service.ISolicitudService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Componente de seguridad para validar acceso a solicitudes basado en roles y propiedad.
 * Usado en @PreAuthorize para autorización a nivel de método.
 */
@Component("solicitudSecurityService")
public class SolicitudSecurityService {
    
    private final ISolicitudService solicitudService;
    
    public SolicitudSecurityService(ISolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }
    
    /**
     * Valida si el usuario autenticado puede acceder a una solicitud específica.
     * COORDINADOR y ADMINISTRATIVO: pueden ver cualquier solicitud.
     * ESTUDIANTE: solo puede ver solicitudes propias.
     */
    public boolean canAccessSolicitud(Long solicitudId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long usuarioId = userDetails.getUsuarioId();
        RolNombre rol = userDetails.getRol();
        
        // COORDINADOR y ADMINISTRATIVO pueden acceder a cualquier solicitud
        if (rol == RolNombre.COORDINADOR || rol == RolNombre.ADMINISTRATIVO) {
            return true;
        }
        
        // ESTUDIANTE solo puede acceder a solicitudes propias
        if (rol == RolNombre.ESTUDIANTE) {
            try {
                Solicitud solicitud = solicitudService.obtenerSolicitud(solicitudId);
                if (solicitud != null && solicitud.getSolicitante() != null) {
                    return solicitud.getSolicitante().getId().equals(usuarioId);
                }
            } catch (Exception e) {
                // Si hay error al obtener solicitud, denegar acceso
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * Valida si el usuario autenticado puede modificar/clasificar una solicitud.
     * Solo COORDINADOR y ADMINISTRATIVO pueden hacerlo.
     */
    public boolean canModifySolicitud(Long solicitudId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        RolNombre rol = userDetails.getRol();
        
        // Solo COORDINADOR y ADMINISTRATIVO pueden modificar
        return rol == RolNombre.COORDINADOR || rol == RolNombre.ADMINISTRATIVO;
    }
    
    /**
     * Valida si el usuario autenticado puede asignar responsables.
     * Solo COORDINADOR puede hacerlo.
     */
    public boolean canAssignResponsable(Long solicitudId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        RolNombre rol = userDetails.getRol();
        
        // Solo COORDINADOR puede asignar
        return rol == RolNombre.COORDINADOR;
    }
    
    /**
     * Valida si el usuario autenticado puede cerrar una solicitud.
     * Solo COORDINADOR puede hacerlo.
     */
    public boolean canCloseSolicitud(Long solicitudId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        RolNombre rol = userDetails.getRol();
        
        // Solo COORDINADOR puede cerrar
        return rol == RolNombre.COORDINADOR;
    }
}
