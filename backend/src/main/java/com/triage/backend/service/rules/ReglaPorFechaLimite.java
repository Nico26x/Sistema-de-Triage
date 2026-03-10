package com.triage.backend.service.rules;

import com.triage.backend.domain.entity.Solicitud;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class ReglaPorFechaLimite implements ReglaPriorizacion {

    @Override
    public int evaluar(Solicitud s) {
        LocalDateTime limite = s.getFechaLimite();
        if (limite == null) return 0;

        long dias = ChronoUnit.DAYS.between(LocalDateTime.now(), limite);

        if (dias <= 1) return 40;
        if (dias <= 3) return 25;
        if (dias <= 7) return 15;
        return 5;
    }

    @Override
    public String explicar(Solicitud s) {
        LocalDateTime limite = s.getFechaLimite();
        if (limite == null) return "FechaLimite=SIN_FECHA(+0)";

        long dias = ChronoUnit.DAYS.between(LocalDateTime.now(), limite);
        int pts = evaluar(s);
        return "FechaLimite=" + dias + "d(+" + pts + ")";
    }
}