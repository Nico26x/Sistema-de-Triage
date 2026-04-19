package com.triage.backend.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triage.backend.domain.enums.*;
import com.triage.backend.service.ISolicitudService;
import com.triage.backend.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Jackson modules for Java 8 date/time support
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Pruebas de capa web para {@link SolicitudController}.
 *
 * Cubre endpoints:
 * - POST /api/v1/solicitudes
 * - GET /api/v1/solicitudes (con filtros)
 * - GET /api/v1/solicitudes/{id}
 *
 * Utiliza MockMvc sin levantar toda la aplicación (sin BD, sin Spring Boot completo).
 * El servicio `ISolicitudService` es mocked manualmente.
 */
@DisplayName("SolicitudController - Pruebas Web")
class SolicitudControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ISolicitudService solicitudService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SolicitudController controller = new SolicitudController(solicitudService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ============================================================================
    // PRUEBAS: POST /api/v1/solicitudes
    // ============================================================================

    @Test
    @DisplayName("POST /api/v1/solicitudes: debe responder 201 Created cuando el request es válido")
    void testCrearSolicitudResponde201Created() throws Exception {
        // Arrange
        SolicitudCreateDTO req = new SolicitudCreateDTO(
            "Solicitud de prueba",
            CanalOrigen.TELEFONICO,
            1L,
            ImpactoAcademico.ALTO,
            LocalDateTime.now().plusDays(7),
            TipoSolicitudNombre.HOMOLOGACION
        );

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            1L,
            "Solicitud de prueba",
            LocalDateTime.now(),
            EstadoSolicitud.REGISTRADA,
            Prioridad.ALTA,
            "Justificación test",
            CanalOrigen.TELEFONICO,
            TipoSolicitudNombre.HOMOLOGACION,
            "Juan Pérez",
            null,
            LocalDateTime.now().plusDays(7),
            ImpactoAcademico.ALTO
        );

        when(solicitudService.crear(any(SolicitudCreateDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/solicitudes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /api/v1/solicitudes: debe devolver el JSON del SolicitudResponseDTO correctamente")
    void testCrearSolicitudDevuelveResponseDTO() throws Exception {
        // Arrange
        SolicitudCreateDTO req = new SolicitudCreateDTO(
            "Nueva solicitud",
            CanalOrigen.CORREO,
            2L,
            ImpactoAcademico.MEDIO,
            LocalDateTime.of(2026, 5, 20, 12, 0),
            TipoSolicitudNombre.CONSULTA_ACADEMICA
        );

        LocalDateTime ahora = LocalDateTime.now();
        SolicitudResponseDTO response = new SolicitudResponseDTO(
            5L,
            "Nueva solicitud",
            ahora,
            EstadoSolicitud.REGISTRADA,
            null,
            null,
            CanalOrigen.CORREO,
            TipoSolicitudNombre.CONSULTA_ACADEMICA,
            "María García",
            null,
            LocalDateTime.of(2026, 5, 20, 12, 0),
            ImpactoAcademico.MEDIO
        );

        when(solicitudService.crear(any(SolicitudCreateDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/solicitudes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(5)))
            .andExpect(jsonPath("$.descripcion", is("Nueva solicitud")))
            .andExpect(jsonPath("$.estado", is("REGISTRADA")))
            .andExpect(jsonPath("$.canalOrigen", is("CORREO")))
            .andExpect(jsonPath("$.tipoSolicitud", is("CONSULTA_ACADEMICA")))
            .andExpect(jsonPath("$.solicitante", is("María García")));
    }

    @Test
    @DisplayName("POST /api/v1/solicitudes: debe invocar solicitudService.crear() con el DTO correcto")
    void testCrearSolicitudInvocaServicio() throws Exception {
        // Arrange
        SolicitudCreateDTO req = new SolicitudCreateDTO(
            "Test description",
            CanalOrigen.SAC,
            3L,
            ImpactoAcademico.BAJO,
            LocalDateTime.of(2026, 6, 1, 10, 0),
            TipoSolicitudNombre.OTRO
        );

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            10L, "Test description", LocalDateTime.now(), EstadoSolicitud.REGISTRADA,
            null, null, CanalOrigen.SAC, TipoSolicitudNombre.OTRO,
            "Test User", null, LocalDateTime.of(2026, 6, 1, 10, 0), ImpactoAcademico.BAJO
        );

        when(solicitudService.crear(any(SolicitudCreateDTO.class))).thenReturn(response);

        ArgumentCaptor<SolicitudCreateDTO> captor = ArgumentCaptor.forClass(SolicitudCreateDTO.class);

        // Act
        mockMvc.perform(post("/api/v1/solicitudes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        // Assert
        verify(solicitudService, times(1)).crear(captor.capture());
        verifyNoMoreInteractions(solicitudService);
        SolicitudCreateDTO capturedDto = captor.getValue();

        assertEquals("Test description", capturedDto.descripcion());
        assertEquals(CanalOrigen.SAC, capturedDto.canal());
        assertEquals(3L, capturedDto.solicitanteId());
        assertEquals(ImpactoAcademico.BAJO, capturedDto.impacto());
        assertEquals(TipoSolicitudNombre.OTRO, capturedDto.tipo());
    }

    // ============================================================================
    // PRUEBAS: GET /api/v1/solicitudes
    // ============================================================================

    @Test
    @DisplayName("GET /api/v1/solicitudes: debe responder 200 OK cuando se consulta la lista")
    void testListarSolicitudesResponde200OK() throws Exception {
        // Arrange
        when(solicitudService.listar(any(SolicitudFilterDTO.class)))
            .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/solicitudes")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/solicitudes: debe devolver correctamente una lista JSON de solicitudes")
    void testListarSolicitudesDevuelveLista() throws Exception {
        // Arrange
        SolicitudResponseDTO solicitud1 = new SolicitudResponseDTO(
            1L, "Solicitud 1", LocalDateTime.now().minusHours(2), EstadoSolicitud.REGISTRADA,
            Prioridad.MEDIA, null, CanalOrigen.TELEFONICO, TipoSolicitudNombre.HOMOLOGACION,
            "Juan", null, LocalDateTime.now().plusDays(5), ImpactoAcademico.ALTO
        );

        SolicitudResponseDTO solicitud2 = new SolicitudResponseDTO(
            2L, "Solicitud 2", LocalDateTime.now().minusHours(1), EstadoSolicitud.ATENDIDA,
            null, null, CanalOrigen.CSU, TipoSolicitudNombre.CONSULTA_ACADEMICA,
            "María", null, LocalDateTime.now().plusDays(3), ImpactoAcademico.MEDIO
        );

        when(solicitudService.listar(any(SolicitudFilterDTO.class)))
            .thenReturn(List.of(solicitud1, solicitud2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/solicitudes")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].descripcion", is("Solicitud 1")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].descripcion", is("Solicitud 2")));
    }

    @Test
    @DisplayName("GET /api/v1/solicitudes: debe mapear correctamente los filtros query params")
    void testListarSolicitudesMapeaFiltros() throws Exception {
        // Arrange
        when(solicitudService.listar(any(SolicitudFilterDTO.class)))
            .thenReturn(List.of());

        ArgumentCaptor<SolicitudFilterDTO> captor = ArgumentCaptor.forClass(SolicitudFilterDTO.class);

        // Act
        mockMvc.perform(get("/api/v1/solicitudes")
            .param("estado", "REGISTRADA")
            .param("prioridad", "ALTA")
            .param("tipoSolicitud", "HOMOLOGACION")
            .param("canalOrigen", "TELEFONICO")
            .param("responsableId", "5")
            .param("desde", "2026-04-01T10:00:00")
            .param("hasta", "2026-04-30T18:00:00")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Assert
        verify(solicitudService, times(1)).listar(captor.capture());
        verifyNoMoreInteractions(solicitudService);
        SolicitudFilterDTO capturedFilter = captor.getValue();

        assertEquals(EstadoSolicitud.REGISTRADA, capturedFilter.estado());
        assertEquals(Prioridad.ALTA, capturedFilter.prioridad());
        assertEquals(TipoSolicitudNombre.HOMOLOGACION, capturedFilter.tipoSolicitud());
        assertEquals(CanalOrigen.TELEFONICO, capturedFilter.canalOrigen());
        assertEquals(5L, capturedFilter.responsableId());
        assertNotNull(capturedFilter.desde());
        assertNotNull(capturedFilter.hasta());
    }

    @Test
    @DisplayName("GET /api/v1/solicitudes: debe tolerar filtros inválidos convirtiéndolos a null sin fallar")
    void testListarSolicitudesToleraFiltrosInvalidos() throws Exception {
        // Arrange
        when(solicitudService.listar(any(SolicitudFilterDTO.class)))
            .thenReturn(List.of());

        ArgumentCaptor<SolicitudFilterDTO> captor = ArgumentCaptor.forClass(SolicitudFilterDTO.class);

        // Act
        mockMvc.perform(get("/api/v1/solicitudes")
            .param("estado", "ESTADO_INVALIDO")
            .param("prioridad", "PRIORIDAD_INVALIDA")
            .param("tipoSolicitud", "TIPO_INVALIDO")
            .param("canalOrigen", "CANAL_INVALIDO")
            .param("desde", "fecha-invalida")
            .param("hasta", "otra-fecha-invalida")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Assert
        verify(solicitudService, times(1)).listar(captor.capture());
        verifyNoMoreInteractions(solicitudService);
        SolicitudFilterDTO capturedFilter = captor.getValue();

        assertNull(capturedFilter.estado());
        assertNull(capturedFilter.prioridad());
        assertNull(capturedFilter.tipoSolicitud());
        assertNull(capturedFilter.canalOrigen());
        assertNull(capturedFilter.desde());
        assertNull(capturedFilter.hasta());
    }

    @Test
    @DisplayName("GET /api/v1/solicitudes: debe invocar solicitudService.listar() con el SolicitudFilterDTO correcto")
    void testListarSolicitudesInvocaServicio() throws Exception {
        // Arrange
        when(solicitudService.listar(any(SolicitudFilterDTO.class)))
            .thenReturn(List.of());

        ArgumentCaptor<SolicitudFilterDTO> captor = ArgumentCaptor.forClass(SolicitudFilterDTO.class);

        // Act
        mockMvc.perform(get("/api/v1/solicitudes")
            .param("estado", "CLASIFICADA")
            .param("responsableId", "10")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Assert
        verify(solicitudService, times(1)).listar(captor.capture());
        verifyNoMoreInteractions(solicitudService);
        SolicitudFilterDTO capturedFilter = captor.getValue();

        assertEquals(EstadoSolicitud.CLASIFICADA, capturedFilter.estado());
        assertEquals(10L, capturedFilter.responsableId());
    }

    // ============================================================================
    // PRUEBAS: GET /api/v1/solicitudes/{id}
    // ============================================================================

    @Test
    @DisplayName("GET /api/v1/solicitudes/{id}: debe responder 200 OK cuando la solicitud existe")
    void testDetalleSolicitudResponde200OK() throws Exception {
        // Arrange
        Long id = 1L;
        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id, "Solicitud encontrada", LocalDateTime.now(), EstadoSolicitud.REGISTRADA,
            Prioridad.MEDIA, null, CanalOrigen.TELEFONICO, TipoSolicitudNombre.HOMOLOGACION,
            "Juan", null, LocalDateTime.now().plusDays(7), ImpactoAcademico.ALTO
        );

        when(solicitudService.detalle(id)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/solicitudes/{id}", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/solicitudes/{id}: debe devolver correctamente el JSON del SolicitudResponseDTO")
    void testDetalleSolicitudDevuelveJSON() throws Exception {
        // Arrange
        Long id = 5L;
        LocalDateTime ahora = LocalDateTime.now();
        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id,
            "Solicitud detallada",
            ahora,
            EstadoSolicitud.ATENDIDA,
            Prioridad.ALTA,
            "Justificación importante",
            CanalOrigen.CORREO,
            TipoSolicitudNombre.OTRO,
            "Carlos López",
            "Responsable Usuario",
            ahora.plusDays(5),
            ImpactoAcademico.MEDIO
        );

        when(solicitudService.detalle(id)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/solicitudes/{id}", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is(5)))
            .andExpect(jsonPath("$.descripcion", is("Solicitud detallada")))
            .andExpect(jsonPath("$.estado", is("ATENDIDA")))
            .andExpect(jsonPath("$.prioridad", is("ALTA")))
            .andExpect(jsonPath("$.justificacionPrioridad", is("Justificación importante")))
            .andExpect(jsonPath("$.canalOrigen", is("CORREO")))
            .andExpect(jsonPath("$.tipoSolicitud", is("OTRO")))
            .andExpect(jsonPath("$.solicitante", is("Carlos López")))
            .andExpect(jsonPath("$.responsable", is("Responsable Usuario")))
            .andExpect(jsonPath("$.impacto", is("MEDIO")));
    }

    @Test
    @DisplayName("GET /api/v1/solicitudes/{id}: debe invocar solicitudService.detalle(id) con el id correcto")
    void testDetalleSolicitudInvocaServicio() throws Exception {
        // Arrange
        Long id = 99L;
        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id, "Test", LocalDateTime.now(), EstadoSolicitud.REGISTRADA,
            null, null, CanalOrigen.TELEFONICO, TipoSolicitudNombre.HOMOLOGACION,
            "Test", null, LocalDateTime.now(), ImpactoAcademico.BAJO
        );

        when(solicitudService.detalle(id)).thenReturn(response);

        // Act
        mockMvc.perform(get("/api/v1/solicitudes/{id}", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Assert
        verify(solicitudService, times(1)).detalle(id);
        verifyNoMoreInteractions(solicitudService);
    }

    // ============================================================================
    // PRUEBAS: PUT /api/v1/solicitudes/{id}/clasificar
    // ============================================================================

    @Test
    @DisplayName("PUT /api/v1/solicitudes/{id}/clasificar: debe responder 200 OK cuando el request es válido")
    void testClasificarSolicitudResponde200OK() throws Exception {
        // Arrange
        Long id = 1L;
        ClasificarDTO req = new ClasificarDTO(
            TipoSolicitudNombre.HOMOLOGACION,
            ImpactoAcademico.ALTO,
            LocalDateTime.of(2026, 5, 20, 12, 0),
            "Clasificación de prueba"
        );

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id,
            "Solicitud clasificada",
            LocalDateTime.now(),
            EstadoSolicitud.CLASIFICADA,
            Prioridad.ALTA,
            null,
            CanalOrigen.TELEFONICO,
            TipoSolicitudNombre.HOMOLOGACION,
            "Juan",
            null,
            LocalDateTime.of(2026, 5, 20, 12, 0),
            ImpactoAcademico.ALTO
        );

        when(solicitudService.clasificar(id, req)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/solicitudes/{id}/clasificar", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify
        verify(solicitudService, times(1)).clasificar(any(Long.class), any(ClasificarDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/solicitudes/{id}/clasificar: debe devolver correctamente el JSON del SolicitudResponseDTO")
    void testClasificarSolicitudDevuelveResponseDTO() throws Exception {
        // Arrange
        Long id = 2L;
        LocalDateTime fechaLimite = LocalDateTime.of(2026, 6, 15, 14, 30);
        LocalDateTime ahora = LocalDateTime.now();

        ClasificarDTO req = new ClasificarDTO(
            TipoSolicitudNombre.SOLICITUD_CUPO,
            ImpactoAcademico.MEDIO,
            fechaLimite,
            "Requiere revisión académica"
        );

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id,
            "Solicitud de cupo",
            ahora,
            EstadoSolicitud.CLASIFICADA,
            Prioridad.MEDIA,
            "Justificación de prioridad",
            CanalOrigen.CSU,
            TipoSolicitudNombre.SOLICITUD_CUPO,
            "María González",
            null,
            fechaLimite,
            ImpactoAcademico.MEDIO
        );

        when(solicitudService.clasificar(id, req)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/solicitudes/{id}/clasificar", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(2)))
            .andExpect(jsonPath("$.descripcion", is("Solicitud de cupo")))
            .andExpect(jsonPath("$.estado", is("CLASIFICADA")))
            .andExpect(jsonPath("$.prioridad", is("MEDIA")))
            .andExpect(jsonPath("$.tipoSolicitud", is("SOLICITUD_CUPO")))
            .andExpect(jsonPath("$.impacto", is("MEDIO")))
            .andExpect(jsonPath("$.solicitante", is("María González")))
            .andExpect(jsonPath("$.canalOrigen", is("CSU")))
            .andExpect(jsonPath("$.justificacionPrioridad", is("Justificación de prioridad")));
    }

    @Test
    @DisplayName("PUT /api/v1/solicitudes/{id}/clasificar: debe invocar solicitudService.clasificar(id, req) correctamente")
    void testClasificarSolicitudInvocaServicio() throws Exception {
        // Arrange
        Long id = 5L;
        LocalDateTime fechaLimite = LocalDateTime.of(2026, 7, 1, 10, 0);

        ClasificarDTO req = new ClasificarDTO(
            TipoSolicitudNombre.CANCELACION_ASIGNATURA,
            ImpactoAcademico.BAJO,
            fechaLimite,
            "Solicitud de cancelación"
        );

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id, "Test", LocalDateTime.now(), EstadoSolicitud.CLASIFICADA,
            null, null, CanalOrigen.CORREO, TipoSolicitudNombre.CANCELACION_ASIGNATURA,
            "Test", null, fechaLimite, ImpactoAcademico.BAJO
        );

        when(solicitudService.clasificar(id, req)).thenReturn(response);

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<ClasificarDTO> dtoCaptor = ArgumentCaptor.forClass(ClasificarDTO.class);

        // Act
        mockMvc.perform(put("/api/v1/solicitudes/{id}/clasificar", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());

        // Assert
        verify(solicitudService, times(1)).clasificar(idCaptor.capture(), dtoCaptor.capture());
        verifyNoMoreInteractions(solicitudService);

        assertEquals(id, idCaptor.getValue());
        ClasificarDTO capturedDto = dtoCaptor.getValue();
        assertEquals(TipoSolicitudNombre.CANCELACION_ASIGNATURA, capturedDto.tipoSolicitud());
        assertEquals(ImpactoAcademico.BAJO, capturedDto.impacto());
        assertEquals(fechaLimite, capturedDto.fechaLimite());
        assertEquals("Solicitud de cancelación", capturedDto.observacion());
    }

    // ============================================================================
    // PRUEBAS: PUT /api/v1/solicitudes/{id}/asignar
    // ============================================================================

    @Test
    @DisplayName("PUT /api/v1/solicitudes/{id}/asignar: debe responder 200 OK cuando el request es válido")
    void testAsignarSolicitudResponde200OK() throws Exception {
        // Arrange
        Long id = 1L;
        Long responsableId = 10L;

        AsignarDTO req = new AsignarDTO(responsableId);

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id,
            "Solicitud asignada",
            LocalDateTime.now(),
            EstadoSolicitud.ATENDIDA,
            Prioridad.ALTA,
            null,
            CanalOrigen.SAC,
            TipoSolicitudNombre.HOMOLOGACION,
            "Pedro López",
            "Responsable User",
            LocalDateTime.now().plusDays(5),
            ImpactoAcademico.ALTO
        );

        when(solicitudService.asignarResponsable(id, req)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/solicitudes/{id}/asignar", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify
        verify(solicitudService, times(1)).asignarResponsable(any(Long.class), any(AsignarDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/solicitudes/{id}/asignar: debe devolver correctamente el JSON del SolicitudResponseDTO")
    void testAsignarSolicitudDevuelveResponseDTO() throws Exception {
        // Arrange
        Long id = 3L;
        Long responsableId = 15L;
        LocalDateTime ahora = LocalDateTime.now();

        AsignarDTO req = new AsignarDTO(responsableId);

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id,
            "Solicitud para asignación",
            ahora,
            EstadoSolicitud.ATENDIDA,
            Prioridad.MEDIA,
            null,
            CanalOrigen.TELEFONICO,
            TipoSolicitudNombre.CONSULTA_ACADEMICA,
            "Ana Smith",
            "Dr. González",
            ahora.plusDays(3),
            ImpactoAcademico.MEDIO
        );

        when(solicitudService.asignarResponsable(id, req)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/solicitudes/{id}/asignar", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(3)))
            .andExpect(jsonPath("$.descripcion", is("Solicitud para asignación")))
            .andExpect(jsonPath("$.estado", is("ATENDIDA")))
            .andExpect(jsonPath("$.responsable", is("Dr. González")))
            .andExpect(jsonPath("$.solicitante", is("Ana Smith")))
            .andExpect(jsonPath("$.tipoSolicitud", is("CONSULTA_ACADEMICA")))
            .andExpect(jsonPath("$.canalOrigen", is("TELEFONICO")));
    }

    @Test
    @DisplayName("PUT /api/v1/solicitudes/{id}/asignar: debe invocar solicitudService.asignarResponsable(id, req) correctamente")
    void testAsignarSolicitudInvocaServicio() throws Exception {
        // Arrange
        Long id = 7L;
        Long responsableId = 20L;

        AsignarDTO req = new AsignarDTO(responsableId);

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id, "Test", LocalDateTime.now(), EstadoSolicitud.ATENDIDA,
            null, null, CanalOrigen.CORREO, TipoSolicitudNombre.OTRO,
            "Test", "Responsable", LocalDateTime.now(), ImpactoAcademico.BAJO
        );

        when(solicitudService.asignarResponsable(id, req)).thenReturn(response);

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<AsignarDTO> dtoCaptor = ArgumentCaptor.forClass(AsignarDTO.class);

        // Act
        mockMvc.perform(put("/api/v1/solicitudes/{id}/asignar", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());

        // Assert
        verify(solicitudService, times(1)).asignarResponsable(idCaptor.capture(), dtoCaptor.capture());
        verifyNoMoreInteractions(solicitudService);

        assertEquals(id, idCaptor.getValue());
        AsignarDTO capturedDto = dtoCaptor.getValue();
        assertEquals(responsableId, capturedDto.responsableId());
    }

    // ============================================================================
    // PRUEBAS: PUT /api/v1/solicitudes/{id}/estado
    // ============================================================================

    @Test
    @DisplayName("PUT /api/v1/solicitudes/{id}/estado: debe responder 200 OK cuando el request es válido")
    void testCambiarEstadoSolicitudResponde200OK() throws Exception {
        // Arrange
        Long id = 1L;
        CambiarEstadoDTO req = new CambiarEstadoDTO(
            EstadoSolicitud.ATENDIDA,
            "Cambio de estado de prueba"
        );

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id,
            "Solicitud con estado modificado",
            LocalDateTime.now(),
            EstadoSolicitud.ATENDIDA,
            Prioridad.ALTA,
            "Justificación",
            CanalOrigen.TELEFONICO,
            TipoSolicitudNombre.HOMOLOGACION,
            "Juan",
            null,
            LocalDateTime.now().plusDays(7),
            ImpactoAcademico.ALTO
        );

        when(solicitudService.cambiarEstado(id, req)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/solicitudes/{id}/estado", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify
        verify(solicitudService, times(1)).cambiarEstado(any(Long.class), any(CambiarEstadoDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/solicitudes/{id}/estado: debe devolver correctamente el JSON del SolicitudResponseDTO")
    void testCambiarEstadoSolicitudDevuelveResponseDTO() throws Exception {
        // Arrange
        Long id = 2L;
        LocalDateTime ahora = LocalDateTime.now();

        CambiarEstadoDTO req = new CambiarEstadoDTO(
            EstadoSolicitud.CLASIFICADA,
            "Requiere revisión"
        );

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id,
            "Solicitud reclasificada",
            ahora,
            EstadoSolicitud.CLASIFICADA,
            Prioridad.MEDIA,
            "Prioridad actualizada",
            CanalOrigen.CSU,
            TipoSolicitudNombre.SOLICITUD_CUPO,
            "Pedro Martínez",
            null,
            ahora.plusDays(5),
            ImpactoAcademico.MEDIO
        );

        when(solicitudService.cambiarEstado(id, req)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/solicitudes/{id}/estado", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(2)))
            .andExpect(jsonPath("$.descripcion", is("Solicitud reclasificada")))
            .andExpect(jsonPath("$.estado", is("CLASIFICADA")))
            .andExpect(jsonPath("$.prioridad", is("MEDIA")))
            .andExpect(jsonPath("$.tipoSolicitud", is("SOLICITUD_CUPO")))
            .andExpect(jsonPath("$.solicitante", is("Pedro Martínez")))
            .andExpect(jsonPath("$.canalOrigen", is("CSU")))
            .andExpect(jsonPath("$.justificacionPrioridad", is("Prioridad actualizada")));
    }

    @Test
    @DisplayName("PUT /api/v1/solicitudes/{id}/estado: debe invocar solicitudService.cambiarEstado(id, req) correctamente")
    void testCambiarEstadoSolicitudInvocaServicio() throws Exception {
        // Arrange
        Long id = 5L;

        CambiarEstadoDTO req = new CambiarEstadoDTO(
            EstadoSolicitud.ATENDIDA,
            "Cambio a ATENDIDA"
        );

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id, "Test", LocalDateTime.now(), EstadoSolicitud.ATENDIDA,
            null, null, CanalOrigen.CORREO, TipoSolicitudNombre.OTRO,
            "Test", null, LocalDateTime.now(), ImpactoAcademico.BAJO
        );

        when(solicitudService.cambiarEstado(id, req)).thenReturn(response);

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<CambiarEstadoDTO> dtoCaptor = ArgumentCaptor.forClass(CambiarEstadoDTO.class);

        // Act
        mockMvc.perform(put("/api/v1/solicitudes/{id}/estado", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());

        // Assert
        verify(solicitudService, times(1)).cambiarEstado(idCaptor.capture(), dtoCaptor.capture());
        verifyNoMoreInteractions(solicitudService);

        assertEquals(id, idCaptor.getValue());
        CambiarEstadoDTO capturedDto = dtoCaptor.getValue();
        assertEquals(EstadoSolicitud.ATENDIDA, capturedDto.nuevoEstado());
        assertEquals("Cambio a ATENDIDA", capturedDto.observacion());
    }

    // ============================================================================
    // PRUEBAS: PUT /api/v1/solicitudes/{id}/cerrar
    // ============================================================================

    @Test
    @DisplayName("PUT /api/v1/solicitudes/{id}/cerrar: debe responder 200 OK cuando el request es válido")
    void testCerrarSolicitudResponde200OK() throws Exception {
        // Arrange
        Long id = 1L;
        CerrarDTO req = new CerrarDTO("Cierre de prueba");

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id,
            "Solicitud cerrada",
            LocalDateTime.now(),
            EstadoSolicitud.CERRADA,
            Prioridad.MEDIA,
            null,
            CanalOrigen.SAC,
            TipoSolicitudNombre.HOMOLOGACION,
            "Carlos",
            "Responsable",
            LocalDateTime.now().plusDays(2),
            ImpactoAcademico.ALTO
        );

        when(solicitudService.cerrar(id, req)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/solicitudes/{id}/cerrar", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify
        verify(solicitudService, times(1)).cerrar(any(Long.class), any(CerrarDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/solicitudes/{id}/cerrar: debe devolver correctamente el JSON del SolicitudResponseDTO")
    void testCerrarSolicitudDevuelveResponseDTO() throws Exception {
        // Arrange
        Long id = 3L;
        LocalDateTime ahora = LocalDateTime.now();

        CerrarDTO req = new CerrarDTO("Solicitud procesada correctamente");

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id,
            "Solicitud cerrada definitivamente",
            ahora,
            EstadoSolicitud.CERRADA,
            Prioridad.BAJA,
            null,
            CanalOrigen.PRESENCIAL,
            TipoSolicitudNombre.CANCELACION_ASIGNATURA,
            "Lucía Rodríguez",
            "Admin",
            ahora.plusDays(1),
            ImpactoAcademico.BAJO
        );

        when(solicitudService.cerrar(id, req)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/solicitudes/{id}/cerrar", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(3)))
            .andExpect(jsonPath("$.descripcion", is("Solicitud cerrada definitivamente")))
            .andExpect(jsonPath("$.estado", is("CERRADA")))
            .andExpect(jsonPath("$.prioridad", is("BAJA")))
            .andExpect(jsonPath("$.responsable", is("Admin")))
            .andExpect(jsonPath("$.solicitante", is("Lucía Rodríguez")))
            .andExpect(jsonPath("$.tipoSolicitud", is("CANCELACION_ASIGNATURA")))
            .andExpect(jsonPath("$.canalOrigen", is("PRESENCIAL")));
    }

    @Test
    @DisplayName("PUT /api/v1/solicitudes/{id}/cerrar: debe invocar solicitudService.cerrar(id, req) correctamente")
    void testCerrarSolicitudInvocaServicio() throws Exception {
        // Arrange
        Long id = 7L;

        CerrarDTO req = new CerrarDTO("Cierre completado");

        SolicitudResponseDTO response = new SolicitudResponseDTO(
            id, "Test", LocalDateTime.now(), EstadoSolicitud.CERRADA,
            null, null, CanalOrigen.TELEFONICO, TipoSolicitudNombre.OTRO,
            "Test", "Test", LocalDateTime.now(), ImpactoAcademico.BAJO
        );

        when(solicitudService.cerrar(id, req)).thenReturn(response);

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<CerrarDTO> dtoCaptor = ArgumentCaptor.forClass(CerrarDTO.class);

        // Act
        mockMvc.perform(put("/api/v1/solicitudes/{id}/cerrar", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());

        // Assert
        verify(solicitudService, times(1)).cerrar(idCaptor.capture(), dtoCaptor.capture());
        verifyNoMoreInteractions(solicitudService);

        assertEquals(id, idCaptor.getValue());
        CerrarDTO capturedDto = dtoCaptor.getValue();
        assertEquals("Cierre completado", capturedDto.observacion());
    }

    // ============================================================================
    // PRUEBAS: GET /api/v1/solicitudes/{id}/historial
    // ============================================================================

    @Test
    @DisplayName("GET /api/v1/solicitudes/{id}/historial: debe responder 200 OK cuando la solicitud es válida")
    void testObtenerHistorialResponde200OK() throws Exception {
        // Arrange
        Long id = 1L;

        when(solicitudService.obtenerHistorial(id))
            .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/solicitudes/{id}/historial", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify
        verify(solicitudService, times(1)).obtenerHistorial(id);
    }

    @Test
    @DisplayName("GET /api/v1/solicitudes/{id}/historial: debe devolver correctamente una lista JSON de HistorialEntryDTO")
    void testObtenerHistorialDevuelveLista() throws Exception {
        // Arrange
        Long id = 2L;
        LocalDateTime ahora = LocalDateTime.now();

        HistorialEntryDTO entrada1 = new HistorialEntryDTO(
            ahora.minusHours(2),
            AccionHistorial.REGISTRO,
            "Solicitud creada",
            null,
            EstadoSolicitud.REGISTRADA,
            1L
        );

        HistorialEntryDTO entrada2 = new HistorialEntryDTO(
            ahora.minusHours(1),
            AccionHistorial.CLASIFICACION,
            "Solicitud clasificada como HOMOLOGACION",
            EstadoSolicitud.REGISTRADA,
            EstadoSolicitud.CLASIFICADA,
            2L
        );

        HistorialEntryDTO entrada3 = new HistorialEntryDTO(
            ahora,
            AccionHistorial.ASIGNACION,
            "Asignada a responsable",
            EstadoSolicitud.CLASIFICADA,
            EstadoSolicitud.ATENDIDA,
            3L
        );

        when(solicitudService.obtenerHistorial(id))
            .thenReturn(List.of(entrada1, entrada2, entrada3));

        // Act & Assert
        mockMvc.perform(get("/api/v1/solicitudes/{id}/historial", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].accion", is("REGISTRO")))
            .andExpect(jsonPath("$[0].observacion", is("Solicitud creada")))
            .andExpect(jsonPath("$[0].estadoNuevo", is("REGISTRADA")))
            .andExpect(jsonPath("$[0].actorId", is(1)))
            .andExpect(jsonPath("$[1].accion", is("CLASIFICACION")))
            .andExpect(jsonPath("$[1].observacion", is("Solicitud clasificada como HOMOLOGACION")))
            .andExpect(jsonPath("$[1].estadoAnterior", is("REGISTRADA")))
            .andExpect(jsonPath("$[1].estadoNuevo", is("CLASIFICADA")))
            .andExpect(jsonPath("$[2].accion", is("ASIGNACION")))
            .andExpect(jsonPath("$[2].observacion", is("Asignada a responsable")))
            .andExpect(jsonPath("$[2].estadoNuevo", is("ATENDIDA")))
            .andExpect(jsonPath("$[2].actorId", is(3)));
    }

    @Test
    @DisplayName("GET /api/v1/solicitudes/{id}/historial: debe devolver una lista vacía cuando el historial está vacío")
    void testObtenerHistorialDevuelveListaVacia() throws Exception {
        // Arrange
        Long id = 5L;

        when(solicitudService.obtenerHistorial(id))
            .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/solicitudes/{id}/historial", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));

        // Verify
        verify(solicitudService, times(1)).obtenerHistorial(id);
    }

    @Test
    @DisplayName("GET /api/v1/solicitudes/{id}/historial: debe invocar solicitudService.obtenerHistorial(id) con el id correcto")
    void testObtenerHistorialInvocaServicio() throws Exception {
        // Arrange
        Long id = 7L;

        when(solicitudService.obtenerHistorial(id))
            .thenReturn(List.of());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);

        // Act
        mockMvc.perform(get("/api/v1/solicitudes/{id}/historial", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Assert
        verify(solicitudService, times(1)).obtenerHistorial(idCaptor.capture());
        verifyNoMoreInteractions(solicitudService);

        assertEquals(id, idCaptor.getValue());
    }
}
