package com.triage.backend.repository;

import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.enums.EstadoSolicitud;
import com.triage.backend.domain.enums.Prioridad;
import com.triage.backend.domain.enums.TipoSolicitudNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long>, JpaSpecificationExecutor<Solicitud> {

    List<Solicitud> findByEstadoActual(EstadoSolicitud estadoActual);

    List<Solicitud> findByPrioridad(Prioridad prioridad);

    List<Solicitud> findByTipoSolicitud(TipoSolicitudNombre tipoSolicitud);

    List<Solicitud> findByResponsable_Id(Long responsableId);

    List<Solicitud> findBySolicitante_Id(Long solicitanteId);
}