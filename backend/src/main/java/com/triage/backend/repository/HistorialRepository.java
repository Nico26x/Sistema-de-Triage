package com.triage.backend.repository;

import com.triage.backend.domain.entity.HistorialSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialRepository extends JpaRepository<HistorialSolicitud, Long> {

    List<HistorialSolicitud> findBySolicitud_IdOrderByFechaHoraAsc(Long solicitudId);
}