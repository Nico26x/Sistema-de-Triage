package com.triage.backend.service;

import com.triage.backend.domain.entity.HistorialSolicitud;
import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.AccionHistorial;
import com.triage.backend.domain.enums.EstadoSolicitud;
import com.triage.backend.web.dto.HistorialEntryDTO;

import java.util.List;

public interface IHistorialService {
    void registrarEvento(Solicitud solicitud, Usuario actor, AccionHistorial accion,
                        String observacion, EstadoSolicitud estadoAnterior,
                        EstadoSolicitud estadoNuevo);
    List<HistorialEntryDTO> listarPorSolicitud(Long solicitudId);
}