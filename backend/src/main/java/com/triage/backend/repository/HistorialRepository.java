package com.triage.backend.repository;


import com.triage.backend.domain.entity.HistorialSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialRepository extends JpaRepository<HistorialSolicitud, Long> {
    
    List<HistorialSolicitud> findBySolicitudIdOrderByFechaHoraAsc(Long solicitudId);
}