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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link SolicitudServiceImpl}.
 * 
 * Cubre métodos `crear(...)` y `detalle(...)` sin usar base de datos,
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
            .prioridad(Prioridad.ALTA).build();

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
}
