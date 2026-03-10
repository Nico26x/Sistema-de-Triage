package com.triage.backend.service.rules;

import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.enums.ImpactoAcademico;
import org.springframework.stereotype.Component;

@Component
public class ReglaPorImpacto implements ReglaPriorizacion {

    @Override
    public int evaluar(Solicitud s) {
        ImpactoAcademico impacto = s.getImpacto();
        if (impacto == null) return 0;

        return switch (impacto) {
            case CRITICO -> 40;
            case ALTO -> 30;
            case MEDIO -> 20;
            case BAJO -> 10;
        };
    }

    @Override
    public String explicar(Solicitud s) {
        int pts = evaluar(s);
        String imp = (s.getImpacto() == null) ? "SIN_IMPACTO" : s.getImpacto().name();
        return "Impacto=" + imp + "(+" + pts + ")";
    }
}