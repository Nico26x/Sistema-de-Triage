package com.triage.backend.service;

import com.triage.backend.web.dto.*;
import java.util.List;

public interface ISolicitudService {
    SolicitudResponseDTO crear(SolicitudCreateDTO dto);
    List<SolicitudResponseDTO> listar(SolicitudFilterDTO filtro);
    SolicitudResponseDTO detalle(Long id);
    SolicitudResponseDTO clasificar(Long id, ClasificarDTO dto);
    SolicitudResponseDTO asignarResponsable(Long id, AsignarDTO dto);
    SolicitudResponseDTO cambiarEstado(Long id, CambiarEstadoDTO dto);
    SolicitudResponseDTO cerrar(Long id, CerrarDTO dto);
    List<HistorialEntryDTO> obtenerHistorial(Long id);
}
