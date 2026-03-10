package com.triage.backend.service;

import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.enums.Prioridad;
import com.triage.backend.service.rules.ReglaPriorizacion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PriorizacionService {

    private final List<ReglaPriorizacion> reglas;

    public PriorizacionService(List<ReglaPriorizacion> reglas) {
        this.reglas = reglas;
    }

    public ResultadoPriorizacion calcular(Solicitud solicitud) {
        int total = reglas.stream().mapToInt(r -> r.evaluar(solicitud)).sum();

        Prioridad prioridad = mapearPrioridad(total);

        String justificacion = reglas.stream()
                .map(r -> r.explicar(solicitud))
                .collect(Collectors.joining(" + "))
                + " => Total=" + total
                + " => Prioridad=" + prioridad.name();

        return new ResultadoPriorizacion(prioridad, justificacion);
    }

    private Prioridad mapearPrioridad(int total) {
        if (total >= 90) return Prioridad.CRITICA;
        if (total >= 60) return Prioridad.ALTA;
        if (total >= 30) return Prioridad.MEDIA;
        return Prioridad.BAJA;
    }
}