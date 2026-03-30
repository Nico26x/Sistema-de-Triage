package com.triage.backend.repository;

import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    
    List<Solicitud> findByEstadoActual(EstadoSolicitud estado);
    
    List<Solicitud> findByPrioridad(Prioridad prioridad);
    
    List<Solicitud> findByTipoSolicitud(TipoSolicitudNombre tipo);
    
    List<Solicitud> findByCanalOrigen(CanalOrigen canal);
    
    List<Solicitud> findByResponsableId(Long responsableId);
    
    List<Solicitud> findBySolicitanteId(Long solicitanteId);
    
    @Query("SELECT s FROM Solicitud s WHERE " +
           "(:estado IS NULL OR s.estadoActual = :estado) AND " +
           "(:prioridad IS NULL OR s.prioridad = :prioridad) AND " +
           "(:tipoSolicitud IS NULL OR s.tipoSolicitud = :tipoSolicitud) AND " +
           "(:canalOrigen IS NULL OR s.canalOrigen = :canalOrigen) AND " +
           "(:responsableId IS NULL OR s.responsable.id = :responsableId) AND " +
           "(:desde IS NULL OR s.fechaRegistro >= :desde) AND " +
           "(:hasta IS NULL OR s.fechaRegistro <= :hasta)")
    List<Solicitud> buscarPorFiltros(
        @Param("estado") EstadoSolicitud estado,
        @Param("prioridad") Prioridad prioridad,
        @Param("tipoSolicitud") TipoSolicitudNombre tipoSolicitud,
        @Param("canalOrigen") CanalOrigen canalOrigen,
        @Param("responsableId") Long responsableId,
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta
    );
}