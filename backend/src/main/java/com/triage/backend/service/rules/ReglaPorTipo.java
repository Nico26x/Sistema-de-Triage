package com.triage.backend.service.rules;

import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.enums.TipoSolicitudNombre;
import org.springframework.stereotype.Component;

@Component
public class ReglaPorTipo implements ReglaPriorizacion {

    @Override
    public int evaluar(Solicitud s) {
        TipoSolicitudNombre tipo = s.getTipoSolicitud();
        if (tipo == null) return 0;

        return switch (tipo) {
            case HOMOLOGACION -> 30;
            case SOLICITUD_CUPO -> 25;
            case CANCELACION_ASIGNATURA -> 20;
            case REGISTRO_ASIGNATURA -> 15;
            case CONSULTA_ACADEMICA -> 10;
            case OTRO -> 5;
        };
    }

    @Override
    public String explicar(Solicitud s) {
        int pts = evaluar(s);
        String tipo = (s.getTipoSolicitud() == null) ? "SIN_TIPO" : s.getTipoSolicitud().name();
        return "Tipo=" + tipo + "(+" + pts + ")";
    }
}