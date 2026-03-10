package com.triage.backend.service;

import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.enums.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class SolicitudSpecifications {

    private SolicitudSpecifications() {}

    public static Specification<Solicitud> estado(EstadoSolicitud estado) {
        return (root, query, cb) -> estado == null ? cb.conjunction() : cb.equal(root.get("estadoActual"), estado);
    }

    public static Specification<Solicitud> prioridad(Prioridad prioridad) {
        return (root, query, cb) -> prioridad == null ? cb.conjunction() : cb.equal(root.get("prioridad"), prioridad);
    }

    public static Specification<Solicitud> tipo(TipoSolicitudNombre tipo) {
        return (root, query, cb) -> tipo == null ? cb.conjunction() : cb.equal(root.get("tipoSolicitud"), tipo);
    }

    public static Specification<Solicitud> canal(CanalOrigen canal) {
        return (root, query, cb) -> canal == null ? cb.conjunction() : cb.equal(root.get("canalOrigen"), canal);
    }

    public static Specification<Solicitud> responsableId(Long responsableId) {
        return (root, query, cb) -> responsableId == null
                ? cb.conjunction()
                : cb.equal(root.get("responsable").get("id"), responsableId);
    }

    public static Specification<Solicitud> desde(LocalDateTime desde) {
        return (root, query, cb) -> desde == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("fechaRegistro"), desde);
    }

    public static Specification<Solicitud> hasta(LocalDateTime hasta) {
        return (root, query, cb) -> hasta == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("fechaRegistro"), hasta);
    }
}