package com.triage.backend.service;

import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.*;
import com.triage.backend.exception.BusinessRuleException;
import com.triage.backend.exception.NotFoundException;
import com.triage.backend.repository.SolicitudRepository;
import com.triage.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final PriorizacionService priorizacionService;
    private final HistorialService historialService;
    private final MaquinaEstadosSolicitud maquinaEstados;

    public SolicitudService(
            SolicitudRepository solicitudRepository,
            UsuarioRepository usuarioRepository,
            PriorizacionService priorizacionService,
            HistorialService historialService,
            MaquinaEstadosSolicitud maquinaEstados
    ) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.priorizacionService = priorizacionService;
        this.historialService = historialService;
        this.maquinaEstados = maquinaEstados;
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────
    private Usuario getActor(Long actorId) {
        return usuarioRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("Usuario actor no encontrado: " + actorId));
    }

    private Solicitud getSolicitud(Long solicitudId) {
        return solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new NotFoundException("Solicitud no encontrada: " + solicitudId));
    }

    private void asegurarNoCerrada(Solicitud s) {
        if (s.getEstadoActual() == EstadoSolicitud.CERRADA) {
            throw new BusinessRuleException("La solicitud está CERRADA y no puede modificarse.");
        }
    }

    private void recalcularPrioridad(Solicitud s) {
        ResultadoPriorizacion rp = priorizacionService.calcular(s);
        s.setPrioridad(rp.getPrioridad());
        s.setJustificacionPrioridad(rp.getJustificacion());
    }

    // ─────────────────────────────────────────────
    // RF-01: Crear solicitud
    // ─────────────────────────────────────────────
    @Transactional
    public Solicitud crear(Solicitud nueva, Long actorId) {
        Usuario actor = getActor(actorId);

        // Solicitante REAL = usuario autenticado (evita IDOR)
        nueva.setSolicitante(actor);

        // Snapshot (no confiar en el front; se llena desde actor real)
        nueva.setIdentificacionSolicitante(actor.getIdentificacion());
        nueva.setNombreSolicitante(actor.getNombre());
        nueva.setEmailSolicitante(actor.getEmail());

        // Estado inicial
        nueva.setEstadoActual(EstadoSolicitud.REGISTRADA);

        // Si llega null, asigna OTRO (coherente con tu enum)
        if (nueva.getTipoSolicitud() == null) {
            nueva.setTipoSolicitud(TipoSolicitudNombre.OTRO);
        }

        // Calcular prioridad inicial + justificación
        recalcularPrioridad(nueva);

        Solicitud guardada = solicitudRepository.save(nueva);

        historialService.registrarEvento(
                guardada,
                actor,
                AccionHistorial.REGISTRO,
                "Registro inicial de la solicitud",
                null,
                guardada.getEstadoActual()
        );

        return guardada;
    }

    // ─────────────────────────────────────────────
    // RF-02 / RF-03: Clasificar (asigna tipo) + recalcula prioridad
    // ─────────────────────────────────────────────
    @Transactional
    public Solicitud clasificar(Long solicitudId, TipoSolicitudNombre tipo, Long actorId) {
        Usuario actor = getActor(actorId);
        Solicitud s = getSolicitud(solicitudId);
        asegurarNoCerrada(s);

        if (tipo == null) {
            throw new BusinessRuleException("El tipo de solicitud es obligatorio para clasificar.");
        }

        EstadoSolicitud anterior = s.getEstadoActual();

        // Transición esperada: REGISTRADA -> CLASIFICADA
        maquinaEstados.validarTransicion(anterior, EstadoSolicitud.CLASIFICADA);

        s.setTipoSolicitud(tipo);
        s.setEstadoActual(EstadoSolicitud.CLASIFICADA);

        // Recalcular prioridad y justificar
        recalcularPrioridad(s);

        Solicitud guardada = solicitudRepository.save(s);

        historialService.registrarEvento(
                guardada,
                actor,
                AccionHistorial.CLASIFICACION,
                "Clasificación: tipo=" + tipo.name(),
                anterior,
                guardada.getEstadoActual()
        );

        historialService.registrarEvento(
                guardada,
                actor,
                AccionHistorial.PRIORIZACION,
                guardada.getJustificacionPrioridad(),
                guardada.getEstadoActual(),
                guardada.getEstadoActual()
        );

        return guardada;
    }

    // ─────────────────────────────────────────────
    // RF-05: Asignar responsable (debe estar activo)
    // ─────────────────────────────────────────────
    @Transactional
    public Solicitud asignarResponsable(Long solicitudId, Long responsableId, Long actorId) {
        Usuario actor = getActor(actorId);
        Solicitud s = getSolicitud(solicitudId);
        asegurarNoCerrada(s);

        Usuario responsable = usuarioRepository.findById(responsableId)
                .orElseThrow(() -> new NotFoundException("Responsable no encontrado: " + responsableId));

        if (!responsable.isActivo()) {
            throw new BusinessRuleException("No se puede asignar: el responsable está inactivo.");
        }

        EstadoSolicitud anterior = s.getEstadoActual();

        // Transición esperada: CLASIFICADA -> EN_ATENCION
        maquinaEstados.validarTransicion(anterior, EstadoSolicitud.EN_ATENCION);

        s.setResponsable(responsable);
        s.setEstadoActual(EstadoSolicitud.EN_ATENCION);

        Solicitud guardada = solicitudRepository.save(s);

        historialService.registrarEvento(
                guardada,
                actor,
                AccionHistorial.ASIGNACION,
                "Asignado responsableId=" + responsableId,
                anterior,
                guardada.getEstadoActual()
        );

        return guardada;
    }

    // ─────────────────────────────────────────────
    // RF-04: Cambiar estado (transiciones válidas)
    // ─────────────────────────────────────────────
    @Transactional
    public Solicitud cambiarEstado(Long solicitudId, EstadoSolicitud nuevoEstado, String observacion, Long actorId) {
        Usuario actor = getActor(actorId);
        Solicitud s = getSolicitud(solicitudId);
        asegurarNoCerrada(s);

        if (nuevoEstado == null) {
            throw new BusinessRuleException("El nuevo estado es obligatorio.");
        }

        EstadoSolicitud anterior = s.getEstadoActual();

        // Si el usuario intenta cerrar por este endpoint, lo bloqueamos:
        // el cierre formal debe ir por cerrar() para exigir observación.
        if (nuevoEstado == EstadoSolicitud.CERRADA) {
            throw new BusinessRuleException("Use el endpoint de cierre para cerrar la solicitud.");
        }

        maquinaEstados.validarTransicion(anterior, nuevoEstado);

        s.setEstadoActual(nuevoEstado);

        Solicitud guardada = solicitudRepository.save(s);

        historialService.registrarEvento(
                guardada,
                actor,
                AccionHistorial.CAMBIO_ESTADO,
                observacion,
                anterior,
                guardada.getEstadoActual()
        );

        return guardada;
    }

    // ─────────────────────────────────────────────
    // RF-08: Cerrar (solo si ATENDIDA y observación obligatoria)
    // ─────────────────────────────────────────────
    @Transactional
    public Solicitud cerrar(Long solicitudId, String observacion, Long actorId) {
        Usuario actor = getActor(actorId);
        Solicitud s = getSolicitud(solicitudId);
        asegurarNoCerrada(s);

        if (observacion == null || observacion.trim().isEmpty()) {
            throw new BusinessRuleException("La observación de cierre es obligatoria.");
        }

        EstadoSolicitud anterior = s.getEstadoActual();

        // Debe estar ATENDIDA para cerrar
        if (anterior != EstadoSolicitud.ATENDIDA) {
            throw new BusinessRuleException("No se puede cerrar: estado actual=" + anterior + ", se requiere ATENDIDA.");
        }

        // Validar transición ATENDIDA -> CERRADA
        maquinaEstados.validarTransicion(anterior, EstadoSolicitud.CERRADA);

        s.setEstadoActual(EstadoSolicitud.CERRADA);

        Solicitud guardada = solicitudRepository.save(s);

        historialService.registrarEvento(
                guardada,
                actor,
                AccionHistorial.CIERRE,
                observacion,
                anterior,
                guardada.getEstadoActual()
        );

        return guardada;
    }

    // ─────────────────────────────────────────────
    // Consultas (base para RF-07)
    // ─────────────────────────────────────────────
@Transactional(readOnly = true)
public Solicitud detalle(Long solicitudId) {
    return getSolicitud(solicitudId);
}

@Transactional(readOnly = true)
public List<Solicitud> listar(
        EstadoSolicitud estado,
        Prioridad prioridad,
        TipoSolicitudNombre tipoSolicitud,
        CanalOrigen canalOrigen,
        Long responsableId,
        java.time.LocalDateTime desde,
        java.time.LocalDateTime hasta
) {
    var spec = SolicitudSpecifications.estado(estado)
            .and(SolicitudSpecifications.prioridad(prioridad))
            .and(SolicitudSpecifications.tipo(tipoSolicitud))
            .and(SolicitudSpecifications.canal(canalOrigen))
            .and(SolicitudSpecifications.responsableId(responsableId))
            .and(SolicitudSpecifications.desde(desde))
            .and(SolicitudSpecifications.hasta(hasta));

    return solicitudRepository.findAll(spec);
}
}