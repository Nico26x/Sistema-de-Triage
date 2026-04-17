package com.triage.backend.service.impl;

import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.*;
import com.triage.backend.exception.BusinessRuleException;
import com.triage.backend.exception.NotFoundException;
import com.triage.backend.repository.SolicitudRepository;
import com.triage.backend.repository.UsuarioRepository;
import com.triage.backend.service.IHistorialService;
import com.triage.backend.service.IMaquinaEstadosSolicitud;
import com.triage.backend.service.IPriorizacionService;
import com.triage.backend.web.dto.SolicitudCreateDTO;
import com.triage.backend.web.dto.SolicitudResponseDTO;
import com.triage.backend.web.dto.ClasificarDTO;
import com.triage.backend.web.dto.AsignarDTO;
import com.triage.backend.web.dto.CambiarEstadoDTO;
import com.triage.backend.web.dto.CerrarDTO;
import com.triage.backend.web.dto.SolicitudFilterDTO;
import com.triage.backend.web.dto.HistorialEntryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link SolicitudServiceImpl}.
 * 
 * Cubre métodos `crear(...)`, `detalle(...)` y `clasificar(...)` sin usar base de datos,
 * Spring completo ni mocks innecesarios. Usa Mockito para todas las dependencias.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SolicitudServiceImpl - Pruebas Unitarias")
class SolicitudServiceImplTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private IHistorialService historialService;

    @Mock
    private IPriorizacionService priorizacionService;

    @Mock
    private IMaquinaEstadosSolicitud maquinaEstados;

    private SolicitudServiceImpl solicitudService;

    @BeforeEach
    void setUp() {
        solicitudService = new SolicitudServiceImpl(
            solicitudRepository,
            usuarioRepository,
            historialService,
            priorizacionService,
            maquinaEstados
        );
    }

    // ============================================================================
    // PRUEBAS: crear(...)
    // ============================================================================

    @Test
    @DisplayName("crear: debe crear correctamente una solicitud con datos válidos")
    void testCrearSolicitudExitosa() {
        Long solicitanteId = 1L;
        String descripcion = "Necesito cambiar de horario";
        ImpactoAcademico impacto = ImpactoAcademico.ALTO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(5);
        TipoSolicitudNombre tipo = TipoSolicitudNombre.SOLICITUD_CUPO;

        SolicitudCreateDTO dto = new SolicitudCreateDTO(
            descripcion, CanalOrigen.CSU, solicitanteId, impacto, fechaLimite, tipo
        );

        Usuario solicitante = Usuario.builder()
            .id(solicitanteId).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(1L).descripcion(descripcion).canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.REGISTRADA).tipoSolicitud(tipo)
            .impacto(impacto).fechaLimite(fechaLimite)
            .solicitante(solicitante).fechaRegistro(LocalDateTime.now())
            .prioridad(Prioridad.ALTA).justificacionPrioridad("Justificación de prioridad")
            .build();

        when(usuarioRepository.findById(solicitanteId)).thenReturn(Optional.of(solicitante));
        when(priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo)).thenReturn(Prioridad.ALTA);
        when(priorizacionService.generarJustificacionPrioridad(impacto, fechaLimite, tipo))
            .thenReturn("Justificación de prioridad");
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        SolicitudResponseDTO resultado = solicitudService.crear(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals(descripcion, resultado.descripcion());
        assertEquals(EstadoSolicitud.REGISTRADA, resultado.estado());
        assertEquals(Prioridad.ALTA, resultado.prioridad());
        assertEquals(CanalOrigen.CSU, resultado.canalOrigen());
        assertEquals(tipo, resultado.tipoSolicitud());
        assertEquals("Juan Pérez", resultado.solicitante());

        verify(usuarioRepository, times(1)).findById(solicitanteId);
        verify(priorizacionService, times(1)).calcularPrioridad(impacto, fechaLimite, tipo);
        verify(solicitudRepository, times(1)).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("crear: debe lanzar NotFoundException cuando el solicitante no existe")
    void testCrearSolicitudSolicitanteNoExiste() {
        Long solicitanteId = 999L;
        SolicitudCreateDTO dto = new SolicitudCreateDTO(
            "Descripción", CanalOrigen.CSU, solicitanteId, 
            ImpactoAcademico.BAJO, LocalDateTime.now().plusDays(5),
            TipoSolicitudNombre.CONSULTA_ACADEMICA
        );

        when(usuarioRepository.findById(solicitanteId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> solicitudService.crear(dto));

        assertEquals("Solicitante no encontrado", exception.getMessage());
        verify(usuarioRepository, times(1)).findById(solicitanteId);
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear: debe lanzar BusinessRuleException cuando el solicitante está inactivo")
    void testCrearSolicitudSolicitanteInactivo() {
        Long solicitanteId = 1L;
        SolicitudCreateDTO dto = new SolicitudCreateDTO(
            "Descripción", CanalOrigen.CSU, solicitanteId,
            ImpactoAcademico.BAJO, LocalDateTime.now().plusDays(5),
            TipoSolicitudNombre.CONSULTA_ACADEMICA
        );

        Usuario solicitanteInactivo = Usuario.builder()
            .id(solicitanteId).nombre("María García").activo(false).build();

        when(usuarioRepository.findById(solicitanteId)).thenReturn(Optional.of(solicitanteInactivo));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.crear(dto));

        assertEquals("El solicitante debe estar activo", exception.getMessage());
        verify(usuarioRepository, times(1)).findById(solicitanteId);
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear: debe asignar correctamente prioridad y justificación")
    void testCrearAsignaPrioridadYJustificacion() {
        Long solicitanteId = 1L;
        ImpactoAcademico impacto = ImpactoAcademico.CRITICO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusHours(6);
        TipoSolicitudNombre tipo = TipoSolicitudNombre.HOMOLOGACION;

        SolicitudCreateDTO dto = new SolicitudCreateDTO(
            "Solicitud crítica", CanalOrigen.CORREO, solicitanteId,
            impacto, fechaLimite, tipo
        );

        Usuario solicitante = Usuario.builder()
            .id(solicitanteId).nombre("Test User").activo(true).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(1L).descripcion("Solicitud crítica").canalOrigen(CanalOrigen.CORREO)
            .estadoActual(EstadoSolicitud.REGISTRADA).tipoSolicitud(tipo)
            .impacto(impacto).fechaLimite(fechaLimite)
            .solicitante(solicitante).fechaRegistro(LocalDateTime.now())
            .prioridad(Prioridad.CRITICA)
            .justificacionPrioridad("Impacto CRITICO: 4pts + Fecha urgente: 4pts + Tipo HOMOLOGACION: 2pts = Total: 10 pts")
            .build();

        when(usuarioRepository.findById(solicitanteId)).thenReturn(Optional.of(solicitante));
        when(priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo)).thenReturn(Prioridad.CRITICA);
        when(priorizacionService.generarJustificacionPrioridad(impacto, fechaLimite, tipo))
            .thenReturn("Impacto CRITICO: 4pts + Fecha urgente: 4pts + Tipo HOMOLOGACION: 2pts = Total: 10 pts");
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        SolicitudResponseDTO resultado = solicitudService.crear(dto);

        assertEquals(Prioridad.CRITICA, resultado.prioridad());
        assertEquals("Impacto CRITICO: 4pts + Fecha urgente: 4pts + Tipo HOMOLOGACION: 2pts = Total: 10 pts",
            resultado.justificacionPrioridad());

        verify(priorizacionService, times(1)).calcularPrioridad(impacto, fechaLimite, tipo);
    }

    @Test
    @DisplayName("crear: debe guardar la solicitud en el repositorio")
    void testCrearGuardaSolicitudEnRepositorio() {
        Long solicitanteId = 1L;
        SolicitudCreateDTO dto = new SolicitudCreateDTO(
            "Nueva solicitud", CanalOrigen.SAC, solicitanteId,
            ImpactoAcademico.MEDIO, LocalDateTime.now().plusDays(3),
            TipoSolicitudNombre.CANCELACION_ASIGNATURA
        );

        Usuario solicitante = Usuario.builder()
            .id(solicitanteId).nombre("Test User").activo(true).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(5L).descripcion("Nueva solicitud").canalOrigen(CanalOrigen.SAC)
            .estadoActual(EstadoSolicitud.REGISTRADA)
            .tipoSolicitud(TipoSolicitudNombre.CANCELACION_ASIGNATURA)
            .impacto(ImpactoAcademico.MEDIO)
            .solicitante(solicitante).fechaRegistro(LocalDateTime.now())
            .prioridad(Prioridad.MEDIA).justificacionPrioridad("Justificación")
            .build();

        when(usuarioRepository.findById(solicitanteId)).thenReturn(Optional.of(solicitante));
        when(priorizacionService.calcularPrioridad(any(), any(), any())).thenReturn(Prioridad.MEDIA);
        when(priorizacionService.generarJustificacionPrioridad(any(), any(), any()))
            .thenReturn("Justificación");
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        SolicitudResponseDTO resultado = solicitudService.crear(dto);

        assertEquals(5L, resultado.id());
        verify(solicitudRepository, times(1)).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("crear: debe registrar evento de historial de registro")
    void testCrearRegistraHistorial() {
        Long solicitanteId = 2L;
        SolicitudCreateDTO dto = new SolicitudCreateDTO(
            "Solicitud con historial", CanalOrigen.CSU, solicitanteId,
            ImpactoAcademico.BAJO, LocalDateTime.now().plusDays(10),
            TipoSolicitudNombre.CONSULTA_ACADEMICA
        );

        Usuario solicitante = Usuario.builder()
            .id(solicitanteId).nombre("Test User").activo(true).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(1L).descripcion("Solicitud con historial").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.REGISTRADA)
            .tipoSolicitud(TipoSolicitudNombre.CONSULTA_ACADEMICA)
            .impacto(ImpactoAcademico.BAJO).solicitante(solicitante)
            .fechaRegistro(LocalDateTime.now()).prioridad(Prioridad.BAJA).build();

        when(usuarioRepository.findById(solicitanteId)).thenReturn(Optional.of(solicitante));
        when(priorizacionService.calcularPrioridad(any(), any(), any())).thenReturn(Prioridad.BAJA);
        when(priorizacionService.generarJustificacionPrioridad(any(), any(), any()))
            .thenReturn("Justificación");
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        solicitudService.crear(dto);

        verify(historialService, times(1)).registrarEvento(
            eq(solicitudGuardada), eq(solicitante), eq(AccionHistorial.REGISTRO),
            eq("Solicitud registrada"), isNull(), eq(EstadoSolicitud.REGISTRADA)
        );
    }

    @Test
    @DisplayName("crear: debe devolver DTO mapeado correctamente")
    void testCrearDevuelveResponseDTOCorrectamente() {
        Long solicitanteId = 1L;
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaLimite = ahora.plusDays(7);

        SolicitudCreateDTO dto = new SolicitudCreateDTO(
            "Solicitud para mapeo", CanalOrigen.TELEFONICO, solicitanteId,
            ImpactoAcademico.ALTO, fechaLimite, TipoSolicitudNombre.HOMOLOGACION
        );

        Usuario solicitante = Usuario.builder()
            .id(solicitanteId).nombre("Carlos López").activo(true).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(10L).descripcion("Solicitud para mapeo").canalOrigen(CanalOrigen.TELEFONICO)
            .estadoActual(EstadoSolicitud.REGISTRADA).tipoSolicitud(TipoSolicitudNombre.HOMOLOGACION)
            .impacto(ImpactoAcademico.ALTO).fechaLimite(fechaLimite)
            .solicitante(solicitante).fechaRegistro(ahora)
            .prioridad(Prioridad.ALTA).justificacionPrioridad("Justificación test")
            .responsable(null).build();

        when(usuarioRepository.findById(solicitanteId)).thenReturn(Optional.of(solicitante));
        when(priorizacionService.calcularPrioridad(any(), any(), any())).thenReturn(Prioridad.ALTA);
        when(priorizacionService.generarJustificacionPrioridad(any(), any(), any()))
            .thenReturn("Justificación test");
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        SolicitudResponseDTO resultado = solicitudService.crear(dto);

        assertAll(
            () -> assertEquals(10L, resultado.id()),
            () -> assertEquals("Solicitud para mapeo", resultado.descripcion()),
            () -> assertEquals(EstadoSolicitud.REGISTRADA, resultado.estado()),
            () -> assertEquals(Prioridad.ALTA, resultado.prioridad()),
            () -> assertEquals("Justificación test", resultado.justificacionPrioridad()),
            () -> assertEquals(CanalOrigen.TELEFONICO, resultado.canalOrigen()),
            () -> assertEquals(TipoSolicitudNombre.HOMOLOGACION, resultado.tipoSolicitud()),
            () -> assertEquals("Carlos López", resultado.solicitante()),
            () -> assertNull(resultado.responsable()),
            () -> assertEquals(fechaLimite, resultado.fechaLimite()),
            () -> assertEquals(ImpactoAcademico.ALTO, resultado.impacto())
        );
    }

    // ============================================================================
    // PRUEBAS: detalle(...)
    // ============================================================================

    @Test
    @DisplayName("detalle: debe devolver correctamente el DTO cuando la solicitud existe")
    void testDetalleRetornaSolicitudCuandoExiste() {
        Long solicitudId = 1L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud de prueba").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.EN_ATENCION).tipoSolicitud(TipoSolicitudNombre.SOLICITUD_CUPO)
            .impacto(ImpactoAcademico.ALTO).fechaLimite(LocalDateTime.now().plusDays(5))
            .solicitante(solicitante).fechaRegistro(LocalDateTime.now())
            .prioridad(Prioridad.ALTA).justificacionPrioridad("Justificación de la solicitud")
            .responsable(null).build();

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        SolicitudResponseDTO resultado = solicitudService.detalle(solicitudId);

        assertNotNull(resultado);
        assertEquals(solicitudId, resultado.id());
        assertEquals("Solicitud de prueba", resultado.descripcion());
        assertEquals(EstadoSolicitud.EN_ATENCION, resultado.estado());
        assertEquals(Prioridad.ALTA, resultado.prioridad());
        assertEquals(CanalOrigen.CSU, resultado.canalOrigen());
        assertEquals(TipoSolicitudNombre.SOLICITUD_CUPO, resultado.tipoSolicitud());
        assertEquals("Juan Pérez", resultado.solicitante());

        verify(solicitudRepository, times(1)).findById(solicitudId);
    }

    @Test
    @DisplayName("detalle: debe lanzar NotFoundException cuando la solicitud no existe")
    void testDetalleLanzaNotFoundExceptionCuandoNoExiste() {
        Long solicitudId = 999L;

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> solicitudService.detalle(solicitudId));

        assertEquals("Solicitud no encontrada", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
    }

    @Test
    @DisplayName("detalle: debe mapear correctamente todos los campos del DTO")
    void testDetalleMapeaCorrectamenteTodosLosCampos() {
        Long solicitudId = 5L;
        LocalDateTime fechaRegistro = LocalDateTime.now().minusDays(2);
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(3);

        Usuario solicitante = Usuario.builder()
            .id(2L).nombre("María García").activo(true).build();

        Usuario responsable = Usuario.builder()
            .id(3L).nombre("Carlos López").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Cambio de horario de clases")
            .canalOrigen(CanalOrigen.PRESENCIAL).estadoActual(EstadoSolicitud.ATENDIDA)
            .tipoSolicitud(TipoSolicitudNombre.CANCELACION_ASIGNATURA)
            .impacto(ImpactoAcademico.MEDIO).fechaLimite(fechaLimite)
            .solicitante(solicitante).fechaRegistro(fechaRegistro)
            .prioridad(Prioridad.MEDIA).justificacionPrioridad("Justificación detallada")
            .responsable(responsable).build();

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        SolicitudResponseDTO resultado = solicitudService.detalle(solicitudId);

        assertAll(
            () -> assertEquals(solicitudId, resultado.id()),
            () -> assertEquals("Cambio de horario de clases", resultado.descripcion()),
            () -> assertEquals(fechaRegistro, resultado.fechaRegistro()),
            () -> assertEquals(EstadoSolicitud.ATENDIDA, resultado.estado()),
            () -> assertEquals(Prioridad.MEDIA, resultado.prioridad()),
            () -> assertEquals("Justificación detallada", resultado.justificacionPrioridad()),
            () -> assertEquals(CanalOrigen.PRESENCIAL, resultado.canalOrigen()),
            () -> assertEquals(TipoSolicitudNombre.CANCELACION_ASIGNATURA, resultado.tipoSolicitud()),
            () -> assertEquals("María García", resultado.solicitante()),
            () -> assertEquals("Carlos López", resultado.responsable()),
            () -> assertEquals(fechaLimite, resultado.fechaLimite()),
            () -> assertEquals(ImpactoAcademico.MEDIO, resultado.impacto())
        );
    }

    @Test
    @DisplayName("detalle: responsable puede ser nulo")
    void testDetalleResponsablePuedeSerNulo() {
        Long solicitudId = 2L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Test User").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud sin responsable")
            .canalOrigen(CanalOrigen.CSU).estadoActual(EstadoSolicitud.REGISTRADA)
            .tipoSolicitud(TipoSolicitudNombre.CONSULTA_ACADEMICA)
            .impacto(ImpactoAcademico.BAJO).solicitante(solicitante)
            .fechaRegistro(LocalDateTime.now()).prioridad(Prioridad.BAJA)
            .responsable(null).build();

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        SolicitudResponseDTO resultado = solicitudService.detalle(solicitudId);

        assertNull(resultado.responsable());
    }

    @Test
    @DisplayName("detalle: debe consultar el repositorio con el ID correcto")
    void testDetalleConsultaRepositorioConIdCorrecto() {
        Long solicitudId = 42L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Test").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.REGISTRADA)
            .tipoSolicitud(TipoSolicitudNombre.CONSULTA_ACADEMICA)
            .solicitante(solicitante).build();

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        solicitudService.detalle(solicitudId);

        verify(solicitudRepository, times(1)).findById(42L);
        verify(solicitudRepository, never()).findById(argThat(id -> !id.equals(42L)));
    }

    // ============================================================================
    // PRUEBAS: clasificar(...)
    // ============================================================================

    @Test
    @DisplayName("clasificar: debe clasificar correctamente cuando todos los datos son válidos")
    void testClasificarSolicitudExitosa() {
        Long solicitudId = 1L;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.HOMOLOGACION;
        ImpactoAcademico impacto = ImpactoAcademico.ALTO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(10);
        String observacion = "Solicitud clasificada";

        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud a clasificar")
            .canalOrigen(CanalOrigen.CSU).estadoActual(EstadoSolicitud.REGISTRADA)
            .solicitante(solicitante).fechaRegistro(LocalDateTime.now())
            .prioridad(null).justificacionPrioridad(null)
            .build();

        Solicitud solicitudClasificada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud a clasificar")
            .canalOrigen(CanalOrigen.CSU).estadoActual(EstadoSolicitud.CLASIFICADA)
            .tipoSolicitud(tipo).impacto(impacto).fechaLimite(fechaLimite)
            .solicitante(solicitante).fechaRegistro(LocalDateTime.now())
            .prioridad(Prioridad.ALTA).justificacionPrioridad("Justificación calculada")
            .build();

        ClasificarDTO dto = new ClasificarDTO(tipo, impacto, fechaLimite, observacion);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo))
            .thenReturn(Prioridad.ALTA);
        when(priorizacionService.generarJustificacionPrioridad(impacto, fechaLimite, tipo))
            .thenReturn("Justificación calculada");
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudClasificada);

        SolicitudResponseDTO resultado = solicitudService.clasificar(solicitudId, dto);

        assertNotNull(resultado);
        assertEquals(solicitudId, resultado.id());
        assertEquals(EstadoSolicitud.CLASIFICADA, resultado.estado());
        assertEquals(tipo, resultado.tipoSolicitud());
        assertEquals(impacto, resultado.impacto());
        assertEquals(fechaLimite, resultado.fechaLimite());
        assertEquals(Prioridad.ALTA, resultado.prioridad());
        assertEquals("Justificación calculada", resultado.justificacionPrioridad());

        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(priorizacionService, times(1)).calcularPrioridad(impacto, fechaLimite, tipo);
        verify(priorizacionService, times(1)).generarJustificacionPrioridad(impacto, fechaLimite, tipo);
        verify(maquinaEstados, times(1)).validarTransicion(EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA);
        verify(solicitudRepository, times(1)).save(any(Solicitud.class));
        verify(historialService, times(1)).registrarEvento(
            eq(solicitudClasificada), isNull(), eq(AccionHistorial.CLASIFICACION),
            eq(observacion), eq(EstadoSolicitud.REGISTRADA), eq(EstadoSolicitud.CLASIFICADA)
        );
    }

    @Test
    @DisplayName("clasificar: debe lanzar NotFoundException cuando la solicitud no existe")
    void testClasificarSolicitudNoExiste() {
        Long solicitudId = 999L;
        ClasificarDTO dto = new ClasificarDTO(
            TipoSolicitudNombre.HOMOLOGACION, ImpactoAcademico.ALTO,
            LocalDateTime.now().plusDays(5), "Observación"
        );

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> solicitudService.clasificar(solicitudId, dto));

        assertEquals("Solicitud no encontrada", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("clasificar: debe lanzar BusinessRuleException cuando no está en estado REGISTRADA")
    void testClasificarSolicitudNoEnRegistrada() {
        Long solicitudId = 1L;
        Usuario solicitante = Usuario.builder().id(1L).nombre("Test").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CLASIFICADA)  // Ya clasificada
            .solicitante(solicitante).build();

        ClasificarDTO dto = new ClasificarDTO(
            TipoSolicitudNombre.HOMOLOGACION, ImpactoAcademico.ALTO,
            LocalDateTime.now().plusDays(5), "Observación"
        );

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.clasificar(solicitudId, dto));

        assertEquals("Solo se pueden clasificar solicitudes en estado REGISTRADA", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
        verify(priorizacionService, never()).calcularPrioridad(any(), any(), any());
        verify(priorizacionService, never()).generarJustificacionPrioridad(any(), any(), any());
        verify(maquinaEstados, never()).validarTransicion(any(), any());
    }

    @Test
    @DisplayName("clasificar: debe lanzar BusinessRuleException cuando tipoSolicitud es null")
    void testClasificarTipoSolicitudNull() {
        Long solicitudId = 1L;
        Usuario solicitante = Usuario.builder().id(1L).nombre("Test").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.REGISTRADA)
            .solicitante(solicitante).build();

        ClasificarDTO dto = new ClasificarDTO(
            null,  // tipoSolicitud es null
            ImpactoAcademico.ALTO,
            LocalDateTime.now().plusDays(5), "Observación"
        );

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.clasificar(solicitudId, dto));

        assertEquals("El tipo de solicitud es requerido", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
        verify(priorizacionService, never()).calcularPrioridad(any(), any(), any());
        verify(priorizacionService, never()).generarJustificacionPrioridad(any(), any(), any());
        verify(maquinaEstados, never()).validarTransicion(any(), any());
    }

    @Test
    @DisplayName("clasificar: debe lanzar BusinessRuleException cuando impacto es null")
    void testClasificarImpactoNull() {
        Long solicitudId = 1L;
        Usuario solicitante = Usuario.builder().id(1L).nombre("Test").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.REGISTRADA)
            .solicitante(solicitante).build();

        ClasificarDTO dto = new ClasificarDTO(
            TipoSolicitudNombre.HOMOLOGACION,
            null,  // impacto es null
            LocalDateTime.now().plusDays(5), "Observación"
        );

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.clasificar(solicitudId, dto));

        assertEquals("El impacto es requerido", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
        verify(priorizacionService, never()).calcularPrioridad(any(), any(), any());
        verify(priorizacionService, never()).generarJustificacionPrioridad(any(), any(), any());
        verify(maquinaEstados, never()).validarTransicion(any(), any());
    }

    @Test
    @DisplayName("clasificar: debe lanzar BusinessRuleException cuando fechaLimite es null")
    void testClasificarFechaLimiteNull() {
        Long solicitudId = 1L;
        Usuario solicitante = Usuario.builder().id(1L).nombre("Test").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.REGISTRADA)
            .solicitante(solicitante).build();

        ClasificarDTO dto = new ClasificarDTO(
            TipoSolicitudNombre.HOMOLOGACION,
            ImpactoAcademico.ALTO,
            null,  // fechaLimite es null
            "Observación"
        );

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.clasificar(solicitudId, dto));

        assertEquals("La fecha límite es requerida", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
        verify(priorizacionService, never()).calcularPrioridad(any(), any(), any());
        verify(priorizacionService, never()).generarJustificacionPrioridad(any(), any(), any());
        verify(maquinaEstados, never()).validarTransicion(any(), any());
    }

    @Test
    @DisplayName("clasificar: debe recalcular prioridad con valores del DTO")
    void testClasificarRecalculaPrioridad() {
        Long solicitudId = 1L;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.SOLICITUD_CUPO;
        ImpactoAcademico impacto = ImpactoAcademico.CRITICO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusHours(2);

        Usuario solicitante = Usuario.builder().id(1L).nombre("Test").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.REGISTRADA).solicitante(solicitante)
            .prioridad(Prioridad.BAJA).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CLASIFICADA).tipoSolicitud(tipo)
            .impacto(impacto).fechaLimite(fechaLimite).solicitante(solicitante)
            .prioridad(Prioridad.CRITICA).build();

        ClasificarDTO dto = new ClasificarDTO(tipo, impacto, fechaLimite, "Obs");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo))
            .thenReturn(Prioridad.CRITICA);
        when(priorizacionService.generarJustificacionPrioridad(impacto, fechaLimite, tipo))
            .thenReturn("Prioridad recalculada");
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        solicitudService.clasificar(solicitudId, dto);

        verify(priorizacionService, times(1)).calcularPrioridad(impacto, fechaLimite, tipo);
    }

    @Test
    @DisplayName("clasificar: debe regenerar justificación con valores del DTO")
    void testClasificarRegeneraJustificacion() {
        Long solicitudId = 1L;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CANCELACION_ASIGNATURA;
        ImpactoAcademico impacto = ImpactoAcademico.MEDIO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(7);

        Usuario solicitante = Usuario.builder().id(1L).nombre("Test").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.REGISTRADA).solicitante(solicitante).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CLASIFICADA).tipoSolicitud(tipo)
            .impacto(impacto).fechaLimite(fechaLimite).solicitante(solicitante)
            .prioridad(Prioridad.MEDIA).justificacionPrioridad("Nueva justificación")
            .build();

        ClasificarDTO dto = new ClasificarDTO(tipo, impacto, fechaLimite, "Obs");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo))
            .thenReturn(Prioridad.MEDIA);
        when(priorizacionService.generarJustificacionPrioridad(impacto, fechaLimite, tipo))
            .thenReturn("Nueva justificación");
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        solicitudService.clasificar(solicitudId, dto);

        verify(priorizacionService, times(1)).generarJustificacionPrioridad(impacto, fechaLimite, tipo);
    }

    @Test
    @DisplayName("clasificar: debe validar transición de estado")
    void testClasificarValidaTransicion() {
        Long solicitudId = 1L;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.HOMOLOGACION;
        ImpactoAcademico impacto = ImpactoAcademico.ALTO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(5);

        Usuario solicitante = Usuario.builder().id(1L).nombre("Test").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.REGISTRADA).solicitante(solicitante).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CLASIFICADA).tipoSolicitud(tipo)
            .impacto(impacto).fechaLimite(fechaLimite).solicitante(solicitante)
            .prioridad(Prioridad.ALTA).build();

        ClasificarDTO dto = new ClasificarDTO(tipo, impacto, fechaLimite, "Obs");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo))
            .thenReturn(Prioridad.ALTA);
        when(priorizacionService.generarJustificacionPrioridad(impacto, fechaLimite, tipo))
            .thenReturn("Justif");
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        solicitudService.clasificar(solicitudId, dto);

        verify(maquinaEstados, times(1))
            .validarTransicion(EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA);
    }

    @Test
    @DisplayName("clasificar: debe propagar BusinessRuleException de validarTransicion")
    void testClasificarPropagaExcepcionValidarTransicion() {
        Long solicitudId = 1L;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.HOMOLOGACION;
        ImpactoAcademico impacto = ImpactoAcademico.ALTO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(5);

        Usuario solicitante = Usuario.builder().id(1L).nombre("Test").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.REGISTRADA).solicitante(solicitante).build();

        ClasificarDTO dto = new ClasificarDTO(tipo, impacto, fechaLimite, "Obs");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo))
            .thenReturn(Prioridad.ALTA);
        when(priorizacionService.generarJustificacionPrioridad(impacto, fechaLimite, tipo))
            .thenReturn("Justif");
        
        // Simular que la transición es inválida
        doThrow(new BusinessRuleException("Transición de estado no permitida"))
            .when(maquinaEstados)
            .validarTransicion(EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.clasificar(solicitudId, dto));

        assertEquals("Transición de estado no permitida", exception.getMessage());
        
        // Verificar que NO se guardó ni se registró historial
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("clasificar: debe cambiar estado a CLASIFICADA usando ArgumentCaptor")
    void testClasificarCambiaEstadoAClasificada() {
        Long solicitudId = 1L;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.HOMOLOGACION;
        ImpactoAcademico impacto = ImpactoAcademico.ALTO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(5);

        Usuario solicitante = Usuario.builder().id(1L).nombre("Test").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.REGISTRADA).solicitante(solicitante).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CLASIFICADA).tipoSolicitud(tipo)
            .impacto(impacto).fechaLimite(fechaLimite).solicitante(solicitante)
            .prioridad(Prioridad.ALTA).justificacionPrioridad("Justif")
            .build();

        ClasificarDTO dto = new ClasificarDTO(tipo, impacto, fechaLimite, "Obs");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo))
            .thenReturn(Prioridad.ALTA);
        when(priorizacionService.generarJustificacionPrioridad(impacto, fechaLimite, tipo))
            .thenReturn("Justif");
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        ArgumentCaptor<Solicitud> captor = ArgumentCaptor.forClass(Solicitud.class);

        solicitudService.clasificar(solicitudId, dto);

        verify(solicitudRepository).save(captor.capture());
        Solicitud solicitudGuardadaCapturada = captor.getValue();

        assertEquals(EstadoSolicitud.CLASIFICADA, solicitudGuardadaCapturada.getEstadoActual());
        assertEquals(tipo, solicitudGuardadaCapturada.getTipoSolicitud());
        assertEquals(impacto, solicitudGuardadaCapturada.getImpacto());
        assertEquals(fechaLimite, solicitudGuardadaCapturada.getFechaLimite());
        assertEquals(Prioridad.ALTA, solicitudGuardadaCapturada.getPrioridad());
        assertEquals("Justif", solicitudGuardadaCapturada.getJustificacionPrioridad());
    }

    @Test
    @DisplayName("clasificar: debe registrar evento de clasificación")
    void testClasificarRegistraEvento() {
        Long solicitudId = 3L;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.SOLICITUD_CUPO;
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(15);
        String observacion = "Clasificada por el sistema";

        Usuario solicitante = Usuario.builder().id(1L).nombre("Test").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.REGISTRADA).solicitante(solicitante).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CLASIFICADA).tipoSolicitud(tipo)
            .impacto(impacto).fechaLimite(fechaLimite).solicitante(solicitante)
            .prioridad(Prioridad.BAJA).build();

        ClasificarDTO dto = new ClasificarDTO(tipo, impacto, fechaLimite, observacion);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo))
            .thenReturn(Prioridad.BAJA);
        when(priorizacionService.generarJustificacionPrioridad(impacto, fechaLimite, tipo))
            .thenReturn("Justif");
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        solicitudService.clasificar(solicitudId, dto);

        verify(historialService, times(1)).registrarEvento(
            eq(solicitudGuardada),
            isNull(),
            eq(AccionHistorial.CLASIFICACION),
            eq(observacion),
            eq(EstadoSolicitud.REGISTRADA),
            eq(EstadoSolicitud.CLASIFICADA)
        );
    }

    @Test
    @DisplayName("clasificar: debe devolver DTO mapeado correctamente")
    void testClasificarDevuelveDTOCorrectamente() {
        Long solicitudId = 5L;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.REGISTRO_ASIGNATURA;
        ImpactoAcademico impacto = ImpactoAcademico.MEDIO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(8);
        LocalDateTime fechaRegistro = LocalDateTime.now();

        Usuario solicitante = Usuario.builder()
            .id(2L).nombre("María García").activo(true).build();

        Usuario responsable = Usuario.builder()
            .id(3L).nombre("Carlos López").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Quiero registrar una asignatura")
            .canalOrigen(CanalOrigen.CORREO)
            .estadoActual(EstadoSolicitud.REGISTRADA)
            .solicitante(solicitante).fechaRegistro(fechaRegistro)
            .responsable(responsable).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(solicitudId).descripcion("Quiero registrar una asignatura")
            .canalOrigen(CanalOrigen.CORREO)
            .estadoActual(EstadoSolicitud.CLASIFICADA)
            .tipoSolicitud(tipo).impacto(impacto).fechaLimite(fechaLimite)
            .solicitante(solicitante).fechaRegistro(fechaRegistro)
            .responsable(responsable).prioridad(Prioridad.MEDIA)
            .justificacionPrioridad("Justificación detallada").build();

        ClasificarDTO dto = new ClasificarDTO(tipo, impacto, fechaLimite, "Observación");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo))
            .thenReturn(Prioridad.MEDIA);
        when(priorizacionService.generarJustificacionPrioridad(impacto, fechaLimite, tipo))
            .thenReturn("Justificación detallada");
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        SolicitudResponseDTO resultado = solicitudService.clasificar(solicitudId, dto);

        assertAll(
            () -> assertEquals(solicitudId, resultado.id()),
            () -> assertEquals("Quiero registrar una asignatura", resultado.descripcion()),
            () -> assertEquals(fechaRegistro, resultado.fechaRegistro()),
            () -> assertEquals(EstadoSolicitud.CLASIFICADA, resultado.estado()),
            () -> assertEquals(Prioridad.MEDIA, resultado.prioridad()),
            () -> assertEquals("Justificación detallada", resultado.justificacionPrioridad()),
            () -> assertEquals(CanalOrigen.CORREO, resultado.canalOrigen()),
            () -> assertEquals(tipo, resultado.tipoSolicitud()),
            () -> assertEquals("María García", resultado.solicitante()),
            () -> assertEquals("Carlos López", resultado.responsable()),
            () -> assertEquals(fechaLimite, resultado.fechaLimite()),
            () -> assertEquals(impacto, resultado.impacto())
        );
    }

    // ============================================================================
    // PRUEBAS: asignarResponsable(...)
    // ============================================================================

    @Test
    @DisplayName("asignarResponsable: debe asignar responsable correctamente cuando todos los datos son válidos")
    void testAsignarResponsableExitosa() {
        Long solicitudId = 1L;
        Long responsableId = 5L;

        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Usuario responsable = Usuario.builder()
            .id(responsableId).nombre("Ana Martínez").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud a asignar").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CLASIFICADA).tipoSolicitud(TipoSolicitudNombre.SOLICITUD_CUPO)
            .impacto(ImpactoAcademico.ALTO).fechaLimite(LocalDateTime.now().plusDays(5))
            .solicitante(solicitante).fechaRegistro(LocalDateTime.now())
            .prioridad(Prioridad.ALTA).justificacionPrioridad("Justificación")
            .responsable(null).build();

        Solicitud solicitudAsignada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud a asignar").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CLASIFICADA).tipoSolicitud(TipoSolicitudNombre.SOLICITUD_CUPO)
            .impacto(ImpactoAcademico.ALTO).fechaLimite(LocalDateTime.now().plusDays(5))
            .solicitante(solicitante).fechaRegistro(LocalDateTime.now())
            .prioridad(Prioridad.ALTA).justificacionPrioridad("Justificación")
            .responsable(responsable).build();

        AsignarDTO dto = new AsignarDTO(responsableId);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(usuarioRepository.findById(responsableId)).thenReturn(Optional.of(responsable));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudAsignada);

        SolicitudResponseDTO resultado = solicitudService.asignarResponsable(solicitudId, dto);

        assertNotNull(resultado);
        assertEquals(solicitudId, resultado.id());
        assertEquals("Ana Martínez", resultado.responsable());
        assertEquals(EstadoSolicitud.CLASIFICADA, resultado.estado());

        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(usuarioRepository, times(1)).findById(responsableId);
        verify(solicitudRepository, times(1)).save(any(Solicitud.class));
        verify(historialService, times(1)).registrarEvento(
            eq(solicitudAsignada), eq(responsable), eq(AccionHistorial.ASIGNACION),
            eq("Asignado a: Ana Martínez"), eq(EstadoSolicitud.CLASIFICADA), eq(EstadoSolicitud.CLASIFICADA)
        );
        verifyNoInteractions(maquinaEstados);
        verifyNoInteractions(priorizacionService);
    }

    @Test
    @DisplayName("asignarResponsable: debe lanzar NotFoundException cuando la solicitud no existe")
    void testAsignarResponsableSolicitudNoExiste() {
        Long solicitudId = 999L;
        Long responsableId = 5L;
        AsignarDTO dto = new AsignarDTO(responsableId);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> solicitudService.asignarResponsable(solicitudId, dto));

        assertEquals("Solicitud no encontrada", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(usuarioRepository, never()).findById(any());
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("asignarResponsable: debe lanzar NotFoundException cuando el responsable no existe")
    void testAsignarResponsableResponsableNoExiste() {
        Long solicitudId = 1L;
        Long responsableId = 999L;

        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CLASIFICADA).solicitante(solicitante)
            .responsable(null).build();

        AsignarDTO dto = new AsignarDTO(responsableId);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(responsableId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> solicitudService.asignarResponsable(solicitudId, dto));

        assertEquals("Responsable no encontrado", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(usuarioRepository, times(1)).findById(responsableId);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("asignarResponsable: debe lanzar BusinessRuleException cuando el responsable está inactivo")
    void testAsignarResponsableResponsableInactivo() {
        Long solicitudId = 1L;
        Long responsableId = 5L;

        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Usuario responsableInactivo = Usuario.builder()
            .id(responsableId).nombre("Ana Martínez").activo(false).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CLASIFICADA).solicitante(solicitante)
            .responsable(null).build();

        AsignarDTO dto = new AsignarDTO(responsableId);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(responsableId)).thenReturn(Optional.of(responsableInactivo));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.asignarResponsable(solicitudId, dto));

        assertEquals("El responsable debe estar activo", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(usuarioRepository, times(1)).findById(responsableId);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("asignarResponsable: debe lanzar BusinessRuleException cuando la solicitud está cerrada")
    void testAsignarResponsableSolicitudCerrada() {
        Long solicitudId = 1L;
        Long responsableId = 5L;

        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitudCerrada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud cerrada").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CERRADA).solicitante(solicitante)
            .responsable(null).build();

        AsignarDTO dto = new AsignarDTO(responsableId);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudCerrada));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.asignarResponsable(solicitudId, dto));

        assertEquals("No se puede asignar responsable a una solicitud cerrada", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(usuarioRepository, never()).findById(any());
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("asignarResponsable: debe guardar la solicitud actualizada en el repositorio")
    void testAsignarResponsableGuardaSolicitud() {
        Long solicitudId = 1L;
        Long responsableId = 5L;

        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Usuario responsable = Usuario.builder()
            .id(responsableId).nombre("Ana Martínez").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.EN_ATENCION).solicitante(solicitante)
            .responsable(null).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.EN_ATENCION).solicitante(solicitante)
            .responsable(responsable).build();

        AsignarDTO dto = new AsignarDTO(responsableId);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(usuarioRepository.findById(responsableId)).thenReturn(Optional.of(responsable));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        solicitudService.asignarResponsable(solicitudId, dto);

        verify(solicitudRepository, times(1)).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("asignarResponsable: debe registrar evento de asignación en historial")
    void testAsignarResponsableRegistraEvento() {
        Long solicitudId = 2L;
        Long responsableId = 6L;

        Usuario solicitante = Usuario.builder()
            .id(2L).nombre("María García").activo(true).build();

        Usuario responsable = Usuario.builder()
            .id(responsableId).nombre("Carlos López").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CORREO)
            .estadoActual(EstadoSolicitud.CLASIFICADA).solicitante(solicitante)
            .responsable(null).build();

        Solicitud solicitudAsignada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CORREO)
            .estadoActual(EstadoSolicitud.CLASIFICADA).solicitante(solicitante)
            .responsable(responsable).build();

        AsignarDTO dto = new AsignarDTO(responsableId);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(usuarioRepository.findById(responsableId)).thenReturn(Optional.of(responsable));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudAsignada);

        solicitudService.asignarResponsable(solicitudId, dto);

        verify(historialService, times(1)).registrarEvento(
            eq(solicitudAsignada), eq(responsable), eq(AccionHistorial.ASIGNACION),
            eq("Asignado a: Carlos López"), eq(EstadoSolicitud.CLASIFICADA), eq(EstadoSolicitud.CLASIFICADA)
        );
    }

    @Test
    @DisplayName("asignarResponsable: debe devolver DTO mapeado correctamente")
    void testAsignarResponsableDevuelveDTOCorrectamente() {
        Long solicitudId = 3L;
        Long responsableId = 7L;
        LocalDateTime fechaRegistro = LocalDateTime.now().minusDays(1);
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(10);

        Usuario solicitante = Usuario.builder()
            .id(3L).nombre("Pedro Rodríguez").activo(true).build();

        Usuario responsable = Usuario.builder()
            .id(responsableId).nombre("Diana Flores").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Cambio de horario")
            .canalOrigen(CanalOrigen.PRESENCIAL).estadoActual(EstadoSolicitud.EN_ATENCION)
            .tipoSolicitud(TipoSolicitudNombre.CANCELACION_ASIGNATURA)
            .impacto(ImpactoAcademico.MEDIO).fechaLimite(fechaLimite)
            .solicitante(solicitante).fechaRegistro(fechaRegistro)
            .prioridad(Prioridad.MEDIA).justificacionPrioridad("Justificación")
            .responsable(null).build();

        Solicitud solicitudAsignada = Solicitud.builder()
            .id(solicitudId).descripcion("Cambio de horario")
            .canalOrigen(CanalOrigen.PRESENCIAL).estadoActual(EstadoSolicitud.EN_ATENCION)
            .tipoSolicitud(TipoSolicitudNombre.CANCELACION_ASIGNATURA)
            .impacto(ImpactoAcademico.MEDIO).fechaLimite(fechaLimite)
            .solicitante(solicitante).fechaRegistro(fechaRegistro)
            .prioridad(Prioridad.MEDIA).justificacionPrioridad("Justificación")
            .responsable(responsable).build();

        AsignarDTO dto = new AsignarDTO(responsableId);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(usuarioRepository.findById(responsableId)).thenReturn(Optional.of(responsable));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudAsignada);

        SolicitudResponseDTO resultado = solicitudService.asignarResponsable(solicitudId, dto);

        assertAll(
            () -> assertEquals(solicitudId, resultado.id()),
            () -> assertEquals("Cambio de horario", resultado.descripcion()),
            () -> assertEquals(fechaRegistro, resultado.fechaRegistro()),
            () -> assertEquals(EstadoSolicitud.EN_ATENCION, resultado.estado()),
            () -> assertEquals(Prioridad.MEDIA, resultado.prioridad()),
            () -> assertEquals("Justificación", resultado.justificacionPrioridad()),
            () -> assertEquals(CanalOrigen.PRESENCIAL, resultado.canalOrigen()),
            () -> assertEquals(TipoSolicitudNombre.CANCELACION_ASIGNATURA, resultado.tipoSolicitud()),
            () -> assertEquals("Pedro Rodríguez", resultado.solicitante()),
            () -> assertEquals("Diana Flores", resultado.responsable()),
            () -> assertEquals(fechaLimite, resultado.fechaLimite()),
            () -> assertEquals(ImpactoAcademico.MEDIO, resultado.impacto())
        );
    }

    @Test
    @DisplayName("asignarResponsable: debe asignar responsable correctamente usando ArgumentCaptor")
    void testAsignarResponsableUsaArgumentCaptor() {
        Long solicitudId = 4L;
        Long responsableId = 8L;

        Usuario solicitante = Usuario.builder()
            .id(4L).nombre("Laura Gómez").activo(true).build();

        Usuario responsable = Usuario.builder()
            .id(responsableId).nombre("Roberto Torres").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud test").canalOrigen(CanalOrigen.SAC)
            .estadoActual(EstadoSolicitud.REGISTRADA).solicitante(solicitante)
            .responsable(null).build();

        Solicitud solicitudAsignada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud test").canalOrigen(CanalOrigen.SAC)
            .estadoActual(EstadoSolicitud.REGISTRADA).solicitante(solicitante)
            .responsable(responsable).build();

        AsignarDTO dto = new AsignarDTO(responsableId);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(usuarioRepository.findById(responsableId)).thenReturn(Optional.of(responsable));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudAsignada);

        ArgumentCaptor<Solicitud> captor = ArgumentCaptor.forClass(Solicitud.class);

        solicitudService.asignarResponsable(solicitudId, dto);

        verify(solicitudRepository).save(captor.capture());
        Solicitud solicitudGuardadaCapturada = captor.getValue();

        assertEquals(responsable, solicitudGuardadaCapturada.getResponsable());
        assertEquals("Roberto Torres", solicitudGuardadaCapturada.getResponsable().getNombre());
    }

    // ============================================================================
    // PRUEBAS: cambiarEstado(...)
    // ============================================================================

    @Test
    @DisplayName("cambiarEstado: debe cambiar correctamente el estado cuando la transición es válida")
    void testCambiarEstadoExitosa() {
        Long solicitudId = 1L;
        EstadoSolicitud estadoActual = EstadoSolicitud.CLASIFICADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.EN_ATENCION;
        String observacion = "Cambiando a en atención";

        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud para cambiar estado").canalOrigen(CanalOrigen.CSU)
            .estadoActual(estadoActual).tipoSolicitud(TipoSolicitudNombre.SOLICITUD_CUPO)
            .impacto(ImpactoAcademico.ALTO).fechaLimite(LocalDateTime.now().plusDays(5))
            .solicitante(solicitante).fechaRegistro(LocalDateTime.now())
            .prioridad(Prioridad.ALTA).justificacionPrioridad("Justificación")
            .responsable(null).build();

        Solicitud solicitudCambiada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud para cambiar estado").canalOrigen(CanalOrigen.CSU)
            .estadoActual(nuevoEstado).tipoSolicitud(TipoSolicitudNombre.SOLICITUD_CUPO)
            .impacto(ImpactoAcademico.ALTO).fechaLimite(LocalDateTime.now().plusDays(5))
            .solicitante(solicitante).fechaRegistro(LocalDateTime.now())
            .prioridad(Prioridad.ALTA).justificacionPrioridad("Justificación")
            .responsable(null).build();

        CambiarEstadoDTO dto = new CambiarEstadoDTO(nuevoEstado, observacion);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudCambiada);

        SolicitudResponseDTO resultado = solicitudService.cambiarEstado(solicitudId, dto);

        assertNotNull(resultado);
        assertEquals(solicitudId, resultado.id());
        assertEquals(nuevoEstado, resultado.estado());

        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(maquinaEstados, times(1)).validarTransicion(estadoActual, nuevoEstado);
        verify(solicitudRepository, times(1)).save(any(Solicitud.class));
        verify(historialService, times(1)).registrarEvento(
            eq(solicitudCambiada), isNull(), eq(AccionHistorial.CAMBIO_ESTADO),
            eq(observacion), eq(estadoActual), eq(nuevoEstado)
        );
        verifyNoInteractions(priorizacionService);
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("cambiarEstado: debe lanzar NotFoundException cuando la solicitud no existe")
    void testCambiarEstadoSolicitudNoExiste() {
        Long solicitudId = 999L;
        CambiarEstadoDTO dto = new CambiarEstadoDTO(EstadoSolicitud.EN_ATENCION, "Observación");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> solicitudService.cambiarEstado(solicitudId, dto));

        assertEquals("Solicitud no encontrada", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(maquinaEstados, never()).validarTransicion(any(), any());
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(priorizacionService);
    }

    @Test
    @DisplayName("cambiarEstado: debe lanzar BusinessRuleException cuando la solicitud está cerrada")
    void testCambiarEstadoSolicitudCerrada() {
        Long solicitudId = 1L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitudCerrada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud cerrada").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CERRADA).solicitante(solicitante)
            .responsable(null).build();

        CambiarEstadoDTO dto = new CambiarEstadoDTO(EstadoSolicitud.ATENDIDA, "Intento de cambio");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudCerrada));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.cambiarEstado(solicitudId, dto));

        assertEquals("No se puede cambiar el estado de una solicitud cerrada", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(maquinaEstados, never()).validarTransicion(any(), any());
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(priorizacionService);
    }

    @Test
    @DisplayName("cambiarEstado: debe propagar BusinessRuleException de validarTransicion")
    void testCambiarEstadoPropagaExcepcionValidarTransicion() {
        Long solicitudId = 1L;
        EstadoSolicitud estadoActual = EstadoSolicitud.REGISTRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.ATENDIDA;

        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(estadoActual).solicitante(solicitante).build();

        CambiarEstadoDTO dto = new CambiarEstadoDTO(nuevoEstado, "Observación");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        doThrow(new BusinessRuleException("Transición inválida de REGISTRADA a ATENDIDA"))
            .when(maquinaEstados)
            .validarTransicion(estadoActual, nuevoEstado);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.cambiarEstado(solicitudId, dto));

        assertEquals("Transición inválida de REGISTRADA a ATENDIDA", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(maquinaEstados, times(1)).validarTransicion(estadoActual, nuevoEstado);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("cambiarEstado: debe guardar la solicitud actualizada en el repositorio")
    void testCambiarEstadoGuardaSolicitud() {
        Long solicitudId = 1L;
        EstadoSolicitud estadoActual = EstadoSolicitud.EN_ATENCION;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.ATENDIDA;

        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CSU)
            .estadoActual(estadoActual).solicitante(solicitante)
            .responsable(null).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CSU)
            .estadoActual(nuevoEstado).solicitante(solicitante)
            .responsable(null).build();

        CambiarEstadoDTO dto = new CambiarEstadoDTO(nuevoEstado, "Cambio de estado");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        solicitudService.cambiarEstado(solicitudId, dto);

        verify(solicitudRepository, times(1)).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("cambiarEstado: debe registrar evento de cambio de estado en historial")
    void testCambiarEstadoRegistraEvento() {
        Long solicitudId = 2L;
        EstadoSolicitud estadoAnterior = EstadoSolicitud.CLASIFICADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.EN_ATENCION;
        String observacion = "Pasando a en atención";

        Usuario solicitante = Usuario.builder()
            .id(2L).nombre("María García").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CORREO)
            .estadoActual(estadoAnterior).solicitante(solicitante)
            .responsable(null).build();

        Solicitud solicitudCambiada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CORREO)
            .estadoActual(nuevoEstado).solicitante(solicitante)
            .responsable(null).build();

        CambiarEstadoDTO dto = new CambiarEstadoDTO(nuevoEstado, observacion);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudCambiada);

        solicitudService.cambiarEstado(solicitudId, dto);

        verify(historialService, times(1)).registrarEvento(
            eq(solicitudCambiada), isNull(), eq(AccionHistorial.CAMBIO_ESTADO),
            eq(observacion), eq(estadoAnterior), eq(nuevoEstado)
        );
    }

    @Test
    @DisplayName("cambiarEstado: debe devolver DTO mapeado correctamente")
    void testCambiarEstadoDevuelveDTOCorrectamente() {
        Long solicitudId = 3L;
        EstadoSolicitud estadoAnterior = EstadoSolicitud.EN_ATENCION;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.ATENDIDA;
        LocalDateTime fechaRegistro = LocalDateTime.now().minusDays(1);
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(5);

        Usuario solicitante = Usuario.builder()
            .id(3L).nombre("Pedro Rodríguez").activo(true).build();

        Usuario responsable = Usuario.builder()
            .id(4L).nombre("Laura Gómez").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud a atender")
            .canalOrigen(CanalOrigen.PRESENCIAL).estadoActual(estadoAnterior)
            .tipoSolicitud(TipoSolicitudNombre.CANCELACION_ASIGNATURA)
            .impacto(ImpactoAcademico.MEDIO).fechaLimite(fechaLimite)
            .solicitante(solicitante).fechaRegistro(fechaRegistro)
            .prioridad(Prioridad.MEDIA).justificacionPrioridad("Justificación")
            .responsable(responsable).build();

        Solicitud solicitudCambiada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud a atender")
            .canalOrigen(CanalOrigen.PRESENCIAL).estadoActual(nuevoEstado)
            .tipoSolicitud(TipoSolicitudNombre.CANCELACION_ASIGNATURA)
            .impacto(ImpactoAcademico.MEDIO).fechaLimite(fechaLimite)
            .solicitante(solicitante).fechaRegistro(fechaRegistro)
            .prioridad(Prioridad.MEDIA).justificacionPrioridad("Justificación")
            .responsable(responsable).build();

        CambiarEstadoDTO dto = new CambiarEstadoDTO(nuevoEstado, "Solicitud atendida");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudCambiada);

        SolicitudResponseDTO resultado = solicitudService.cambiarEstado(solicitudId, dto);

        assertAll(
            () -> assertEquals(solicitudId, resultado.id()),
            () -> assertEquals("Solicitud a atender", resultado.descripcion()),
            () -> assertEquals(fechaRegistro, resultado.fechaRegistro()),
            () -> assertEquals(nuevoEstado, resultado.estado()),
            () -> assertEquals(Prioridad.MEDIA, resultado.prioridad()),
            () -> assertEquals("Justificación", resultado.justificacionPrioridad()),
            () -> assertEquals(CanalOrigen.PRESENCIAL, resultado.canalOrigen()),
            () -> assertEquals(TipoSolicitudNombre.CANCELACION_ASIGNATURA, resultado.tipoSolicitud()),
            () -> assertEquals("Pedro Rodríguez", resultado.solicitante()),
            () -> assertEquals("Laura Gómez", resultado.responsable()),
            () -> assertEquals(fechaLimite, resultado.fechaLimite()),
            () -> assertEquals(ImpactoAcademico.MEDIO, resultado.impacto())
        );
    }

    @Test
    @DisplayName("cambiarEstado: debe cambiar estado correctamente usando ArgumentCaptor")
    void testCambiarEstadoUsaArgumentCaptor() {
        Long solicitudId = 4L;
        EstadoSolicitud estadoActual = EstadoSolicitud.CLASIFICADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.EN_ATENCION;
        String descripcionOriginal = "Solicitud test";

        Usuario solicitante = Usuario.builder()
            .id(4L).nombre("Carlos López").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion(descripcionOriginal).canalOrigen(CanalOrigen.SAC)
            .estadoActual(estadoActual).solicitante(solicitante)
            .responsable(null).build();

        Solicitud solicitudCambiada = Solicitud.builder()
            .id(solicitudId).descripcion(descripcionOriginal).canalOrigen(CanalOrigen.SAC)
            .estadoActual(nuevoEstado).solicitante(solicitante)
            .responsable(null).build();

        CambiarEstadoDTO dto = new CambiarEstadoDTO(nuevoEstado, "Cambio de estado test");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudCambiada);

        ArgumentCaptor<Solicitud> captor = ArgumentCaptor.forClass(Solicitud.class);

        solicitudService.cambiarEstado(solicitudId, dto);

        verify(solicitudRepository).save(captor.capture());
        Solicitud solicitudGuardadaCapturada = captor.getValue();

        assertEquals(nuevoEstado, solicitudGuardadaCapturada.getEstadoActual());
        assertEquals(descripcionOriginal, solicitudGuardadaCapturada.getDescripcion());
        assertEquals(solicitante, solicitudGuardadaCapturada.getSolicitante());
    }

    // ============================================================================
    // PRUEBAS: cerrar(...)
    // ============================================================================

    @Test
    @DisplayName("cerrar: debe cerrar correctamente una solicitud cuando está en ATENDIDA")
    void testCerrarSolicitudExitosa() {
        Long solicitudId = 1L;
        String observacion = "Solicitud resuelta satisfactoriamente";

        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud para cerrar").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.ATENDIDA).tipoSolicitud(TipoSolicitudNombre.SOLICITUD_CUPO)
            .impacto(ImpactoAcademico.ALTO).fechaLimite(LocalDateTime.now().plusDays(5))
            .solicitante(solicitante).fechaRegistro(LocalDateTime.now())
            .prioridad(Prioridad.ALTA).justificacionPrioridad("Justificación")
            .responsable(null).build();

        Solicitud solicitudCerrada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud para cerrar").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CERRADA).tipoSolicitud(TipoSolicitudNombre.SOLICITUD_CUPO)
            .impacto(ImpactoAcademico.ALTO).fechaLimite(LocalDateTime.now().plusDays(5))
            .solicitante(solicitante).fechaRegistro(LocalDateTime.now())
            .prioridad(Prioridad.ALTA).justificacionPrioridad("Justificación")
            .responsable(null).build();

        CerrarDTO dto = new CerrarDTO(observacion);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudCerrada);

        SolicitudResponseDTO resultado = solicitudService.cerrar(solicitudId, dto);

        assertNotNull(resultado);
        assertEquals(solicitudId, resultado.id());
        assertEquals(EstadoSolicitud.CERRADA, resultado.estado());

        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(solicitudRepository, times(1)).save(any(Solicitud.class));
        verify(historialService, times(1)).registrarEvento(
            eq(solicitudCerrada), isNull(), eq(AccionHistorial.CIERRE),
            eq(observacion), eq(EstadoSolicitud.ATENDIDA), eq(EstadoSolicitud.CERRADA)
        );
        verifyNoInteractions(maquinaEstados);
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(priorizacionService);
    }

    @Test
    @DisplayName("cerrar: debe lanzar NotFoundException cuando la solicitud no existe")
    void testCerrarSolicitudNoExiste() {
        Long solicitudId = 999L;
        CerrarDTO dto = new CerrarDTO("Cierre de prueba");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> solicitudService.cerrar(solicitudId, dto));

        assertEquals("Solicitud no encontrada", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
        verifyNoInteractions(maquinaEstados);
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(priorizacionService);
    }

    @Test
    @DisplayName("cerrar: debe lanzar BusinessRuleException cuando la solicitud ya está cerrada")
    void testCerrarSolicitudYaCerrada() {
        Long solicitudId = 1L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitudCerrada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud cerrada").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CERRADA).solicitante(solicitante)
            .responsable(null).build();

        CerrarDTO dto = new CerrarDTO("Intento de cierre");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudCerrada));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.cerrar(solicitudId, dto));

        assertEquals("La solicitud ya está cerrada", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
        verifyNoInteractions(maquinaEstados);
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(priorizacionService);
    }

    @Test
    @DisplayName("cerrar: debe lanzar BusinessRuleException cuando la solicitud no está en ATENDIDA")
    void testCerrarSolicitudNoEnAtendida() {
        Long solicitudId = 1L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitudEnClasificada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CLASIFICADA).solicitante(solicitante)
            .responsable(null).build();

        CerrarDTO dto = new CerrarDTO("Intento de cierre");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudEnClasificada));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.cerrar(solicitudId, dto));

        assertEquals("Solo se pueden cerrar solicitudes en estado ATENDIDA", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
        verifyNoInteractions(maquinaEstados);
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(priorizacionService);
    }

    @Test
    @DisplayName("cerrar: debe lanzar BusinessRuleException cuando observación es null")
    void testCerrarObservacionNull() {
        Long solicitudId = 1L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.ATENDIDA).solicitante(solicitante)
            .responsable(null).build();

        CerrarDTO dto = new CerrarDTO(null);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.cerrar(solicitudId, dto));

        assertEquals("La observación de cierre es obligatoria", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
        verifyNoInteractions(maquinaEstados);
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(priorizacionService);
    }

    @Test
    @DisplayName("cerrar: debe lanzar BusinessRuleException cuando observación está vacía")
    void testCerrarObservacionVacia() {
        Long solicitudId = 1L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.ATENDIDA).solicitante(solicitante)
            .responsable(null).build();

        CerrarDTO dto = new CerrarDTO("");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.cerrar(solicitudId, dto));

        assertEquals("La observación de cierre es obligatoria", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
        verifyNoInteractions(maquinaEstados);
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(priorizacionService);
    }

    @Test
    @DisplayName("cerrar: debe lanzar BusinessRuleException cuando observación contiene solo espacios")
    void testCerrarObservacionSoloEspacios() {
        Long solicitudId = 1L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.ATENDIDA).solicitante(solicitante)
            .responsable(null).build();

        CerrarDTO dto = new CerrarDTO("   ");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
            () -> solicitudService.cerrar(solicitudId, dto));

        assertEquals("La observación de cierre es obligatoria", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(solicitudRepository, never()).save(any());
        verify(historialService, never()).registrarEvento(any(), any(), any(), any(), any(), any());
        verifyNoInteractions(maquinaEstados);
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(priorizacionService);
    }

    @Test
    @DisplayName("cerrar: debe guardar la solicitud actualizada en el repositorio")
    void testCerrarGuardaSolicitud() {
        Long solicitudId = 1L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.ATENDIDA).solicitante(solicitante)
            .responsable(null).build();

        Solicitud solicitudGuardada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.CERRADA).solicitante(solicitante)
            .responsable(null).build();

        CerrarDTO dto = new CerrarDTO("Cierre exitoso");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        solicitudService.cerrar(solicitudId, dto);

        verify(solicitudRepository, times(1)).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("cerrar: debe registrar evento de cierre en historial")
    void testCerrarRegistraEvento() {
        Long solicitudId = 2L;
        EstadoSolicitud estadoAnterior = EstadoSolicitud.ATENDIDA;
        String observacion = "Solicitud atendida correctamente";

        Usuario solicitante = Usuario.builder()
            .id(2L).nombre("María García").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CORREO)
            .estadoActual(estadoAnterior).solicitante(solicitante)
            .responsable(null).build();

        Solicitud solicitudCerrada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud").canalOrigen(CanalOrigen.CORREO)
            .estadoActual(EstadoSolicitud.CERRADA).solicitante(solicitante)
            .responsable(null).build();

        CerrarDTO dto = new CerrarDTO(observacion);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudCerrada);

        solicitudService.cerrar(solicitudId, dto);

        verify(historialService, times(1)).registrarEvento(
            eq(solicitudCerrada), isNull(), eq(AccionHistorial.CIERRE),
            eq(observacion), eq(estadoAnterior), eq(EstadoSolicitud.CERRADA)
        );
    }

    @Test
    @DisplayName("cerrar: debe devolver DTO mapeado correctamente")
    void testCerrarDevuelveDTOCorrectamente() {
        Long solicitudId = 3L;
        LocalDateTime fechaRegistro = LocalDateTime.now().minusDays(1);
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(5);

        Usuario solicitante = Usuario.builder()
            .id(3L).nombre("Pedro Rodríguez").activo(true).build();

        Usuario responsable = Usuario.builder()
            .id(4L).nombre("Laura Gómez").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud para cerrar")
            .canalOrigen(CanalOrigen.PRESENCIAL).estadoActual(EstadoSolicitud.ATENDIDA)
            .tipoSolicitud(TipoSolicitudNombre.CANCELACION_ASIGNATURA)
            .impacto(ImpactoAcademico.MEDIO).fechaLimite(fechaLimite)
            .solicitante(solicitante).fechaRegistro(fechaRegistro)
            .prioridad(Prioridad.MEDIA).justificacionPrioridad("Justificación")
            .responsable(responsable).build();

        Solicitud solicitudCerrada = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud para cerrar")
            .canalOrigen(CanalOrigen.PRESENCIAL).estadoActual(EstadoSolicitud.CERRADA)
            .tipoSolicitud(TipoSolicitudNombre.CANCELACION_ASIGNATURA)
            .impacto(ImpactoAcademico.MEDIO).fechaLimite(fechaLimite)
            .solicitante(solicitante).fechaRegistro(fechaRegistro)
            .prioridad(Prioridad.MEDIA).justificacionPrioridad("Justificación")
            .responsable(responsable).build();

        CerrarDTO dto = new CerrarDTO("Cierre ejecutado");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudCerrada);

        SolicitudResponseDTO resultado = solicitudService.cerrar(solicitudId, dto);

        assertAll(
            () -> assertEquals(solicitudId, resultado.id()),
            () -> assertEquals("Solicitud para cerrar", resultado.descripcion()),
            () -> assertEquals(fechaRegistro, resultado.fechaRegistro()),
            () -> assertEquals(EstadoSolicitud.CERRADA, resultado.estado()),
            () -> assertEquals(Prioridad.MEDIA, resultado.prioridad()),
            () -> assertEquals("Justificación", resultado.justificacionPrioridad()),
            () -> assertEquals(CanalOrigen.PRESENCIAL, resultado.canalOrigen()),
            () -> assertEquals(TipoSolicitudNombre.CANCELACION_ASIGNATURA, resultado.tipoSolicitud()),
            () -> assertEquals("Pedro Rodríguez", resultado.solicitante()),
            () -> assertEquals("Laura Gómez", resultado.responsable()),
            () -> assertEquals(fechaLimite, resultado.fechaLimite()),
            () -> assertEquals(ImpactoAcademico.MEDIO, resultado.impacto())
        );
    }

    @Test
    @DisplayName("cerrar: debe cambiar estado a CERRADA correctamente usando ArgumentCaptor")
    void testCerrarUsaArgumentCaptor() {
        Long solicitudId = 4L;
        String descripcionOriginal = "Solicitud test";

        Usuario solicitante = Usuario.builder()
            .id(4L).nombre("Carlos López").activo(true).build();

        Solicitud solicitudInicial = Solicitud.builder()
            .id(solicitudId).descripcion(descripcionOriginal).canalOrigen(CanalOrigen.SAC)
            .estadoActual(EstadoSolicitud.ATENDIDA).solicitante(solicitante)
            .responsable(null).build();

        Solicitud solicitudCerrada = Solicitud.builder()
            .id(solicitudId).descripcion(descripcionOriginal).canalOrigen(CanalOrigen.SAC)
            .estadoActual(EstadoSolicitud.CERRADA).solicitante(solicitante)
            .responsable(null).build();

        CerrarDTO dto = new CerrarDTO("Cierre de prueba");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudInicial));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudCerrada);

        ArgumentCaptor<Solicitud> captor = ArgumentCaptor.forClass(Solicitud.class);

        solicitudService.cerrar(solicitudId, dto);

        verify(solicitudRepository).save(captor.capture());
        Solicitud solicitudGuardadaCapturada = captor.getValue();

        assertEquals(EstadoSolicitud.CERRADA, solicitudGuardadaCapturada.getEstadoActual());
        assertEquals(descripcionOriginal, solicitudGuardadaCapturada.getDescripcion());
        assertEquals(solicitante, solicitudGuardadaCapturada.getSolicitante());
    }

    // ============================================================================
    // PRUEBAS: listar(...)
    // ============================================================================

    @Test
    @DisplayName("listar: debe devolver correctamente una lista de SolicitudResponseDTO")
    void testListarRetornaListaSolicitudes() {
        EstadoSolicitud estado = EstadoSolicitud.CLASIFICADA;
        Prioridad prioridad = Prioridad.MEDIA;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.SOLICITUD_CUPO;
        CanalOrigen canal = CanalOrigen.CSU;
        Long responsableId = 2L;
        LocalDateTime desde = LocalDateTime.now().minusDays(10);
        LocalDateTime hasta = LocalDateTime.now();

        SolicitudFilterDTO filtro = new SolicitudFilterDTO(estado, prioridad, tipo, canal, responsableId, desde, hasta);

        Usuario solicitante1 = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();
        Usuario responsable1 = Usuario.builder()
            .id(2L).nombre("María García").activo(true).build();

        Solicitud solicitud1 = Solicitud.builder()
            .id(1L).descripcion("Solicitud 1").canalOrigen(canal)
            .estadoActual(estado).tipoSolicitud(tipo).impacto(ImpactoAcademico.ALTO)
            .solicitante(solicitante1).fechaRegistro(desde).prioridad(prioridad)
            .responsable(responsable1).build();

        Usuario solicitante2 = Usuario.builder()
            .id(3L).nombre("Carlos López").activo(true).build();

        Solicitud solicitud2 = Solicitud.builder()
            .id(2L).descripcion("Solicitud 2").canalOrigen(canal)
            .estadoActual(estado).tipoSolicitud(tipo).impacto(ImpactoAcademico.MEDIO)
            .solicitante(solicitante2).fechaRegistro(desde.plusDays(2)).prioridad(prioridad)
            .responsable(responsable1).build();

        List<Solicitud> solicitudes = Arrays.asList(solicitud1, solicitud2);

        when(solicitudRepository.buscarPorFiltros(estado, prioridad, tipo, canal, responsableId, desde, hasta))
            .thenReturn(solicitudes);

        List<SolicitudResponseDTO> resultado = solicitudService.listar(filtro);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).id());
        assertEquals("Solicitud 1", resultado.get(0).descripcion());
        assertEquals(2L, resultado.get(1).id());
        assertEquals("Solicitud 2", resultado.get(1).descripcion());

        verify(solicitudRepository, times(1)).buscarPorFiltros(estado, prioridad, tipo, canal, responsableId, desde, hasta);
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(historialService);
        verifyNoInteractions(priorizacionService);
        verifyNoInteractions(maquinaEstados);
    }

    @Test
    @DisplayName("listar: debe devolver una lista vacía cuando el repositorio retorna vacío")
    void testListarRetornaListaVacia() {
        EstadoSolicitud estado = EstadoSolicitud.ATENDIDA;
        Prioridad prioridad = null;
        TipoSolicitudNombre tipo = null;
        CanalOrigen canal = null;
        Long responsableId = null;
        LocalDateTime desde = null;
        LocalDateTime hasta = null;

        SolicitudFilterDTO filtro = new SolicitudFilterDTO(estado, prioridad, tipo, canal, responsableId, desde, hasta);

        when(solicitudRepository.buscarPorFiltros(estado, prioridad, tipo, canal, responsableId, desde, hasta))
            .thenReturn(Collections.emptyList());

        List<SolicitudResponseDTO> resultado = solicitudService.listar(filtro);

        assertNotNull(resultado);
        assertEquals(0, resultado.size());

        verify(solicitudRepository, times(1)).buscarPorFiltros(estado, prioridad, tipo, canal, responsableId, desde, hasta);
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(historialService);
        verifyNoInteractions(priorizacionService);
        verifyNoInteractions(maquinaEstados);
    }

    @Test
    @DisplayName("listar: debe llamar al repositorio pasando correctamente todos los filtros")
    void testListarPasaFiltrosCorrectamente() {
        EstadoSolicitud estado = EstadoSolicitud.REGISTRADA;
        Prioridad prioridad = Prioridad.ALTA;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.HOMOLOGACION;
        CanalOrigen canal = CanalOrigen.CORREO;
        Long responsableId = 5L;
        LocalDateTime desde = LocalDateTime.now().minusDays(30);
        LocalDateTime hasta = LocalDateTime.now().plusDays(10);

        SolicitudFilterDTO filtro = new SolicitudFilterDTO(estado, prioridad, tipo, canal, responsableId, desde, hasta);

        when(solicitudRepository.buscarPorFiltros(estado, prioridad, tipo, canal, responsableId, desde, hasta))
            .thenReturn(Collections.emptyList());

        solicitudService.listar(filtro);

        verify(solicitudRepository, times(1)).buscarPorFiltros(
            eq(estado), eq(prioridad), eq(tipo), eq(canal), eq(responsableId), eq(desde), eq(hasta)
        );
    }

    @Test
    @DisplayName("listar: debe mapear correctamente los campos de cada solicitud al DTO")
    void testListarMapeaCamposCorrectamente() {
        Usuario solicitante = Usuario.builder()
            .id(10L).nombre("Pedro Rodríguez").activo(true).build();
        Usuario responsable = Usuario.builder()
            .id(11L).nombre("Laura Gómez").activo(true).build();

        LocalDateTime fechaRegistro = LocalDateTime.now().minusDays(5);
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(15);

        Solicitud solicitud = Solicitud.builder()
            .id(99L).descripcion("Solicitud para mapeo")
            .canalOrigen(CanalOrigen.PRESENCIAL).estadoActual(EstadoSolicitud.ATENDIDA)
            .tipoSolicitud(TipoSolicitudNombre.CANCELACION_ASIGNATURA)
            .impacto(ImpactoAcademico.BAJO).fechaLimite(fechaLimite)
            .solicitante(solicitante).fechaRegistro(fechaRegistro)
            .prioridad(Prioridad.BAJA).justificacionPrioridad("Justificación de prueba")
            .responsable(responsable).build();

        SolicitudFilterDTO filtro = new SolicitudFilterDTO(null, null, null, null, null, null, null);

        when(solicitudRepository.buscarPorFiltros(null, null, null, null, null, null, null))
            .thenReturn(Arrays.asList(solicitud));

        List<SolicitudResponseDTO> resultado = solicitudService.listar(filtro);

        assertEquals(1, resultado.size());
        SolicitudResponseDTO dto = resultado.get(0);

        assertAll(
            () -> assertEquals(99L, dto.id()),
            () -> assertEquals("Solicitud para mapeo", dto.descripcion()),
            () -> assertEquals(fechaRegistro, dto.fechaRegistro()),
            () -> assertEquals(EstadoSolicitud.ATENDIDA, dto.estado()),
            () -> assertEquals(Prioridad.BAJA, dto.prioridad()),
            () -> assertEquals("Justificación de prueba", dto.justificacionPrioridad()),
            () -> assertEquals(CanalOrigen.PRESENCIAL, dto.canalOrigen()),
            () -> assertEquals(TipoSolicitudNombre.CANCELACION_ASIGNATURA, dto.tipoSolicitud()),
            () -> assertEquals("Pedro Rodríguez", dto.solicitante()),
            () -> assertEquals("Laura Gómez", dto.responsable()),
            () -> assertEquals(fechaLimite, dto.fechaLimite()),
            () -> assertEquals(ImpactoAcademico.BAJO, dto.impacto())
        );
    }

    @Test
    @DisplayName("listar: debe no interactuar con mocks que el método no utiliza")
    void testListarNoInteractuaConMocksNoUtilizados() {
        SolicitudFilterDTO filtro = new SolicitudFilterDTO(
            EstadoSolicitud.REGISTRADA, null, null, null, null, null, null
        );

        when(solicitudRepository.buscarPorFiltros(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        solicitudService.listar(filtro);

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(historialService);
        verifyNoInteractions(priorizacionService);
        verifyNoInteractions(maquinaEstados);
    }

    // ============================================================================
    // PRUEBAS: obtenerHistorial(...)
    // ============================================================================

    @Test
    @DisplayName("obtenerHistorial: debe devolver correctamente la lista de historial cuando la solicitud existe")
    void testObtenerHistorialRetornaListaWhenSolicitudExists() {
        Long solicitudId = 1L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Juan Pérez").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud de prueba").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.EN_ATENCION).tipoSolicitud(TipoSolicitudNombre.SOLICITUD_CUPO)
            .impacto(ImpactoAcademico.ALTO).solicitante(solicitante)
            .fechaRegistro(LocalDateTime.now()).prioridad(Prioridad.ALTA)
            .build();

        HistorialEntryDTO entrada1 = new HistorialEntryDTO(
            LocalDateTime.now().minusDays(2), AccionHistorial.REGISTRO,
            "Solicitud registrada", null, EstadoSolicitud.REGISTRADA, 1L
        );

        HistorialEntryDTO entrada2 = new HistorialEntryDTO(
            LocalDateTime.now().minusHours(6), AccionHistorial.CLASIFICACION,
            "Solicitud clasificada", EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA, null
        );

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(historialService.listarPorSolicitud(solicitudId))
            .thenReturn(Arrays.asList(entrada1, entrada2));

        List<HistorialEntryDTO> resultado = solicitudService.obtenerHistorial(solicitudId);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(AccionHistorial.REGISTRO, resultado.get(0).accion());
        assertEquals(AccionHistorial.CLASIFICACION, resultado.get(1).accion());

        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(historialService, times(1)).listarPorSolicitud(solicitudId);
    }

    @Test
    @DisplayName("obtenerHistorial: debe lanzar NotFoundException cuando la solicitud no existe")
    void testObtenerHistorialLanzaNotFoundExceptionWhenSolicitudNoExists() {
        Long solicitudId = 999L;

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> solicitudService.obtenerHistorial(solicitudId));

        assertEquals("Solicitud no encontrada", exception.getMessage());
        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(historialService, never()).listarPorSolicitud(any());
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(priorizacionService);
        verifyNoInteractions(maquinaEstados);
    }

    @Test
    @DisplayName("obtenerHistorial: debe delegar correctamente en historialService usando solicitud encontrada")
    void testObtenerHistorialDelegatesCorrectlyToHistorialService() {
        Long solicitudId = 5L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Test User").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Test solicitud").canalOrigen(CanalOrigen.SAC)
            .estadoActual(EstadoSolicitud.REGISTRADA).solicitante(solicitante)
            .fechaRegistro(LocalDateTime.now()).build();

        HistorialEntryDTO entrada = new HistorialEntryDTO(
            LocalDateTime.now(), AccionHistorial.REGISTRO, "Registro", null,
            EstadoSolicitud.REGISTRADA, 1L
        );

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(historialService.listarPorSolicitud(solicitudId))
            .thenReturn(Arrays.asList(entrada));

        solicitudService.obtenerHistorial(solicitudId);

        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(historialService, times(1)).listarPorSolicitud(eq(solicitudId));
    }

    @Test
    @DisplayName("obtenerHistorial: debe devolver una lista vacía cuando historialService retorna vacío")
    void testObtenerHistorialRetornsEmptyListWhenHistorialServiceReturnsEmpty() {
        Long solicitudId = 3L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Test User").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Nueva solicitud").canalOrigen(CanalOrigen.PRESENCIAL)
            .estadoActual(EstadoSolicitud.REGISTRADA).solicitante(solicitante)
            .fechaRegistro(LocalDateTime.now()).build();

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(historialService.listarPorSolicitud(solicitudId))
            .thenReturn(Collections.emptyList());

        List<HistorialEntryDTO> resultado = solicitudService.obtenerHistorial(solicitudId);

        assertNotNull(resultado);
        assertEquals(0, resultado.size());

        verify(solicitudRepository, times(1)).findById(solicitudId);
        verify(historialService, times(1)).listarPorSolicitud(solicitudId);
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(priorizacionService);
        verifyNoInteractions(maquinaEstados);
    }

    @Test
    @DisplayName("obtenerHistorial: debe no interactuar con mocks que el método no utiliza")
    void testObtenerHistorialNoInteractuaConMocksNoUtilizados() {
        Long solicitudId = 2L;
        Usuario solicitante = Usuario.builder()
            .id(1L).nombre("Test User").activo(true).build();

        Solicitud solicitud = Solicitud.builder()
            .id(solicitudId).descripcion("Solicitud test").canalOrigen(CanalOrigen.CSU)
            .estadoActual(EstadoSolicitud.REGISTRADA).solicitante(solicitante)
            .fechaRegistro(LocalDateTime.now()).build();

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(historialService.listarPorSolicitud(solicitudId))
            .thenReturn(Collections.emptyList());

        solicitudService.obtenerHistorial(solicitudId);

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(priorizacionService);
        verifyNoInteractions(maquinaEstados);
    }
}
