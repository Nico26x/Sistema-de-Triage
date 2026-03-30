package com.triage.backend.service.impl;

import com.triage.backend.service.IPriorizacionService;
import com.triage.backend.domain.enums.Prioridad;
import com.triage.backend.domain.enums.ImpactoAcademico;
import com.triage.backend.domain.enums.TipoSolicitudNombre;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Transactional(readOnly = true)
public class PriorizacionServiceImpl implements IPriorizacionService {
    
    @Override
    public Prioridad calcularPrioridad(ImpactoAcademico impacto, LocalDateTime fechaLimite, 
                                        TipoSolicitudNombre tipo) {
        int puntuacion = calcularPuntuacion(impacto, fechaLimite, tipo);
        
        if (puntuacion >= 10) {
            return Prioridad.CRITICA;
        } else if (puntuacion >= 7) {
            return Prioridad.ALTA;
        } else if (puntuacion >= 4) {
            return Prioridad.MEDIA;
        } else {
            return Prioridad.BAJA;
        }
    }
    
    @Override
    public int calcularPuntuacion(ImpactoAcademico impacto, LocalDateTime fechaLimite, 
                                   TipoSolicitudNombre tipo) {
        int puntuacion = 0;
        
        // Regla 1: Impacto Académico
        puntuacion += calcularPuntosImpacto(impacto);
        
        // Regla 2: Fecha Límite
        puntuacion += calcularPuntosFechaLimite(fechaLimite);
        
        // Regla 3: Tipo de Solicitud
        puntuacion += calcularPuntosTipo(tipo);
        
        return puntuacion;
    }
    
    private int calcularPuntosImpacto(ImpactoAcademico impacto) {
        return switch (impacto) {
            case BAJO -> 1;
            case MEDIO -> 2;
            case ALTO -> 3;
            case CRITICO -> 4;
            default -> 0;
        };
    }
    
    private int calcularPuntosFechaLimite(LocalDateTime fechaLimite) {
        if (fechaLimite == null) {
            return 0;
        }
        
        long diasRestantes = ChronoUnit.DAYS.between(LocalDateTime.now(), fechaLimite);
        
        if (diasRestantes <= 1) {
            return 4;
        } else if (diasRestantes <= 7) {
            return 3;
        } else if (diasRestantes <= 30) {
            return 2;
        } else {
            return 1;
        }
    }
    
    private int calcularPuntosTipo(TipoSolicitudNombre tipo) {
        return switch (tipo) {
            case HOMOLOGACION, SOLICITUD_CUPO -> 2;
            case CANCELACION_ASIGNATURA, REGISTRO_ASIGNATURA -> 1;
            case CONSULTA_ACADEMICA, OTRO -> 0;
            default -> 0;
        };
    }
}
