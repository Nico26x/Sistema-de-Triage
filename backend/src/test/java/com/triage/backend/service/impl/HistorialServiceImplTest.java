package com.triage.backend.service.impl;

import com.triage.backend.domain.entity.HistorialSolicitud;
import com.triage.backend.domain.entity.Solicitud;
import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.AccionHistorial;
import com.triage.backend.domain.enums.EstadoSolicitud;
import com.triage.backend.domain.enums.RolNombre;
import com.triage.backend.repository.HistorialRepository;
import com.triage.backend.web.dto.HistorialEntryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link HistorialServiceImpl}.
 *
 * Cubre métodos `registrarEvento(...)` y `listarPorSolicitud(...)` sin usar
 * base de datos, Spring completo ni mocks innecesarios. Usa Mockito para
 * todas las dependencias.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HistorialServiceImpl - Pruebas Unitarias")
class HistorialServiceImplTest {

    @Mock
    private HistorialRepository historialRepository;

    private HistorialServiceImpl historialService;

    @BeforeEach
    void setUp() {
        historialService = new HistorialServiceImpl(historialRepository);
    }

    // ============================================================================
    // PRUEBAS: registrarEvento(...)
    // ============================================================================

    @Test
    @DisplayName("registrarEvento: debe guardar correctamente un evento con todos los datos válidos")
    void testRegistrarEventoGuardaCorrectamente() {
        // Arrange
        Solicitud solicitud = Solicitud.builder()
            .id(1L)
            .descripcion("Solicitud de test")
            .build();

        Usuario actor = Usuario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@example.com")
            .build();

        AccionHistorial accion = AccionHistorial.REGISTRO;
        String observacion = "Solicitud registrada exitosamente";
        EstadoSolicitud estadoAnterior = null;
        EstadoSolicitud estadoNuevo = EstadoSolicitud.REGISTRADA;

        ArgumentCaptor<HistorialSolicitud> captor = ArgumentCaptor.forClass(HistorialSolicitud.class);

        // Act
        historialService.registrarEvento(solicitud, actor, accion, observacion, estadoAnterior, estadoNuevo);

        // Assert & Verify
        verify(historialRepository, times(1)).save(captor.capture());
        HistorialSolicitud guardado = captor.getValue();

        assertNotNull(guardado);
        assertEquals(solicitud, guardado.getSolicitud());
        assertEquals(actor, guardado.getActor());
        assertEquals(accion, guardado.getAccion());
        assertEquals(observacion, guardado.getObservacion());
        assertNull(guardado.getEstadoAnterior());
        assertEquals(EstadoSolicitud.REGISTRADA, guardado.getEstadoNuevo());
        assertNotNull(guardado.getFechaHora());
    }

    @Test
    @DisplayName("registrarEvento: debe guardar correctamente cuando el actor es null")
    void testRegistrarEventoConActorNull() {
        // Arrange
        Solicitud solicitud = Solicitud.builder()
            .id(2L)
            .descripcion("Otra solicitud")
            .build();

        AccionHistorial accion = AccionHistorial.CAMBIO_ESTADO;
        String observacion = "Estado actualizado automáticamente";
        EstadoSolicitud estadoAnterior = EstadoSolicitud.REGISTRADA;
        EstadoSolicitud estadoNuevo = EstadoSolicitud.ATENDIDA;

        ArgumentCaptor<HistorialSolicitud> captor = ArgumentCaptor.forClass(HistorialSolicitud.class);

        // Act
        historialService.registrarEvento(solicitud, null, accion, observacion, estadoAnterior, estadoNuevo);

        // Assert & Verify
        verify(historialRepository, times(1)).save(captor.capture());
        HistorialSolicitud guardado = captor.getValue();

        assertNotNull(guardado);
        assertEquals(solicitud, guardado.getSolicitud());
        assertNull(guardado.getActor());
        assertEquals(accion, guardado.getAccion());
        assertEquals(observacion, guardado.getObservacion());
        assertEquals(EstadoSolicitud.REGISTRADA, guardado.getEstadoAnterior());
        assertEquals(EstadoSolicitud.ATENDIDA, guardado.getEstadoNuevo());
        assertNotNull(guardado.getFechaHora());
    }

    @Test
    @DisplayName("registrarEvento: debe guardar con actorId cuando el actor no es null")
    void testRegistrarEventoConActorNoNull() {
        // Arrange
        Solicitud solicitud = Solicitud.builder().id(3L).build();
        Usuario actor = Usuario.builder()
            .id(5L)
            .nombre("María García")
            .build();

        ArgumentCaptor<HistorialSolicitud> captor = ArgumentCaptor.forClass(HistorialSolicitud.class);

        // Act
        historialService.registrarEvento(
            solicitud,
            actor,
            AccionHistorial.ASIGNACION,
            "Asignado a responsable",
            EstadoSolicitud.REGISTRADA,
            EstadoSolicitud.REGISTRADA
        );

        // Assert & Verify
        verify(historialRepository, times(1)).save(captor.capture());
        HistorialSolicitud guardado = captor.getValue();

        assertNotNull(guardado.getActor());
        assertEquals(5L, guardado.getActor().getId());
    }

    @Test
    @DisplayName("registrarEvento: debe persistir correctamente solicitud, acción, observación y estados")
    void testRegistrarEventoPersisteCamposCorrectos() {
        // Arrange
        Solicitud solicitud = Solicitud.builder()
            .id(10L)
            .descripcion("Solicitud para prueba")
            .build();

        Usuario actor = Usuario.builder()
            .id(2L)
            .nombre("Carlos López")
            .build();

        ArgumentCaptor<HistorialSolicitud> captor = ArgumentCaptor.forClass(HistorialSolicitud.class);

        // Act
        historialService.registrarEvento(
            solicitud,
            actor,
            AccionHistorial.CLASIFICACION,
            "Clasificada correctamente",
            null,
            EstadoSolicitud.CLASIFICADA
        );

        // Assert & Verify
        verify(historialRepository, times(1)).save(captor.capture());
        HistorialSolicitud guardado = captor.getValue();

        assertEquals(solicitud.getId(), guardado.getSolicitud().getId());
        assertEquals(AccionHistorial.CLASIFICACION, guardado.getAccion());
        assertEquals("Clasificada correctamente", guardado.getObservacion());
        assertNull(guardado.getEstadoAnterior());
        assertEquals(EstadoSolicitud.CLASIFICADA, guardado.getEstadoNuevo());
    }

    @Test
    @DisplayName("registrarEvento: debe guardar con fechaHora no nula")
    void testRegistrarEventoGuardaFechaHoraNoNula() {
        // Arrange
        Solicitud solicitud = Solicitud.builder().id(4L).build();
        Usuario actor = Usuario.builder().id(1L).build();

        ArgumentCaptor<HistorialSolicitud> captor = ArgumentCaptor.forClass(HistorialSolicitud.class);
        LocalDateTime antesDeRegistro = LocalDateTime.now();

        // Act
        historialService.registrarEvento(
            solicitud,
            actor,
            AccionHistorial.REGISTRO,
            "Evento de prueba",
            null,
            EstadoSolicitud.REGISTRADA
        );

        LocalDateTime despuesDeRegistro = LocalDateTime.now();

        // Assert & Verify
        verify(historialRepository, times(1)).save(captor.capture());
        HistorialSolicitud guardado = captor.getValue();

        assertNotNull(guardado.getFechaHora());
        assertFalse(guardado.getFechaHora().isBefore(antesDeRegistro));
        assertFalse(guardado.getFechaHora().isAfter(despuesDeRegistro.plusSeconds(1)));
    }

    // ============================================================================
    // PRUEBAS: listarPorSolicitud(Long solicitudId)
    // ============================================================================

    @Test
    @DisplayName("listarPorSolicitud: debe devolver correctamente una lista de HistorialEntryDTO")
    void testListarPorSolicitudRetornaListaDTO() {
        // Arrange
        Long solicitudId = 1L;

        HistorialSolicitud historial1 = HistorialSolicitud.builder()
            .id(1L)
            .solicitud(Solicitud.builder().id(solicitudId).build())
            .fechaHora(LocalDateTime.now().minusHours(2))
            .accion(AccionHistorial.REGISTRO)
            .observacion("Solicitud registrada")
            .estadoAnterior(null)
            .estadoNuevo(EstadoSolicitud.REGISTRADA)
            .actor(Usuario.builder().id(1L).build())
            .build();

        HistorialSolicitud historial2 = HistorialSolicitud.builder()
            .id(2L)
            .solicitud(Solicitud.builder().id(solicitudId).build())
            .fechaHora(LocalDateTime.now().minusHours(1))
            .accion(AccionHistorial.CLASIFICACION)
            .observacion("Solicitud clasificada")
            .estadoAnterior(EstadoSolicitud.REGISTRADA)
            .estadoNuevo(EstadoSolicitud.CLASIFICADA)
            .actor(Usuario.builder().id(2L).build())
            .build();

        List<HistorialSolicitud> eventos = List.of(historial1, historial2);
        when(historialRepository.findBySolicitudIdOrderByFechaHoraAsc(solicitudId)).thenReturn(eventos);

        // Act
        List<HistorialEntryDTO> resultado = historialService.listarPorSolicitud(solicitudId);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        HistorialEntryDTO dto1 = resultado.get(0);
        assertEquals(AccionHistorial.REGISTRO, dto1.accion());
        assertEquals("Solicitud registrada", dto1.observacion());
        assertEquals(EstadoSolicitud.REGISTRADA, dto1.estadoNuevo());
        assertEquals(1L, dto1.actorId());

        HistorialEntryDTO dto2 = resultado.get(1);
        assertEquals(AccionHistorial.CLASIFICACION, dto2.accion());
        assertEquals("Solicitud clasificada", dto2.observacion());
        assertEquals(EstadoSolicitud.CLASIFICADA, dto2.estadoNuevo());
        assertEquals(2L, dto2.actorId());

        // Verify interactions
        verify(historialRepository, times(1)).findBySolicitudIdOrderByFechaHoraAsc(solicitudId);
    }

    @Test
    @DisplayName("listarPorSolicitud: debe devolver una lista vacía cuando el repositorio retorna vacío")
    void testListarPorSolicitudRetornaListaVacia() {
        // Arrange
        Long solicitudId = 999L;
        when(historialRepository.findBySolicitudIdOrderByFechaHoraAsc(solicitudId)).thenReturn(new ArrayList<>());

        // Act
        List<HistorialEntryDTO> resultado = historialService.listarPorSolicitud(solicitudId);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        assertEquals(0, resultado.size());

        // Verify interactions
        verify(historialRepository, times(1)).findBySolicitudIdOrderByFechaHoraAsc(solicitudId);
        verifyNoMoreInteractions(historialRepository);
    }

    @Test
    @DisplayName("listarPorSolicitud: debe mapear correctamente todos los campos de HistorialSolicitud a DTO")
    void testListarPorSolicitudMapeaCamposCorrectamente() {
        // Arrange
        Long solicitudId = 7L;
        LocalDateTime fechaHora = LocalDateTime.of(2026, 4, 19, 10, 30, 0);

        HistorialSolicitud historial = HistorialSolicitud.builder()
            .id(100L)
            .solicitud(Solicitud.builder().id(solicitudId).build())
            .fechaHora(fechaHora)
            .accion(AccionHistorial.PRIORIZACION)
            .observacion("Prioridad asignada: ALTA")
            .estadoAnterior(EstadoSolicitud.CLASIFICADA)
            .estadoNuevo(EstadoSolicitud.CLASIFICADA)
            .actor(Usuario.builder()
                .id(3L)
                .nombre("Admin")
                .email("admin@example.com")
                .rol(RolNombre.ADMINISTRATIVO)
                .build())
            .build();

        when(historialRepository.findBySolicitudIdOrderByFechaHoraAsc(solicitudId))
            .thenReturn(List.of(historial));

        // Act
        List<HistorialEntryDTO> resultado = historialService.listarPorSolicitud(solicitudId);

        // Assert
        assertEquals(1, resultado.size());
        HistorialEntryDTO dto = resultado.get(0);

        assertEquals(fechaHora, dto.fechaHora());
        assertEquals(AccionHistorial.PRIORIZACION, dto.accion());
        assertEquals("Prioridad asignada: ALTA", dto.observacion());
        assertEquals(EstadoSolicitud.CLASIFICADA, dto.estadoAnterior());
        assertEquals(EstadoSolicitud.CLASIFICADA, dto.estadoNuevo());
        assertEquals(3L, dto.actorId());

        // Verify interactions
        verify(historialRepository, times(1)).findBySolicitudIdOrderByFechaHoraAsc(solicitudId);
    }

    @Test
    @DisplayName("listarPorSolicitud: debe mapear actorId como null cuando el actor es null")
    void testListarPorSolicitudMapeaActorIdNull() {
        // Arrange
        Long solicitudId = 8L;

        HistorialSolicitud historial = HistorialSolicitud.builder()
            .id(101L)
            .solicitud(Solicitud.builder().id(solicitudId).build())
            .fechaHora(LocalDateTime.now())
            .accion(AccionHistorial.CAMBIO_ESTADO)
            .observacion("Estado cambió automáticamente")
            .estadoAnterior(EstadoSolicitud.REGISTRADA)
            .estadoNuevo(EstadoSolicitud.ATENDIDA)
            .actor(null)
            .build();

        when(historialRepository.findBySolicitudIdOrderByFechaHoraAsc(solicitudId))
            .thenReturn(List.of(historial));

        // Act
        List<HistorialEntryDTO> resultado = historialService.listarPorSolicitud(solicitudId);

        // Assert
        assertEquals(1, resultado.size());
        HistorialEntryDTO dto = resultado.get(0);

        assertNull(dto.actorId());
        assertEquals(AccionHistorial.CAMBIO_ESTADO, dto.accion());

        // Verify interactions
        verify(historialRepository, times(1)).findBySolicitudIdOrderByFechaHoraAsc(solicitudId);
    }

    @Test
    @DisplayName("listarPorSolicitud: debe respetar el orden ascendente por fechaHora del repositorio")
    void testListarPorSolicitudMantieneFechaHoraAscendente() {
        // Arrange
        Long solicitudId = 6L;
        LocalDateTime fecha1 = LocalDateTime.of(2026, 4, 19, 8, 0, 0);
        LocalDateTime fecha2 = LocalDateTime.of(2026, 4, 19, 10, 30, 0);
        LocalDateTime fecha3 = LocalDateTime.of(2026, 4, 19, 14, 15, 0);

        HistorialSolicitud historial1 = HistorialSolicitud.builder()
            .fechaHora(fecha1)
            .accion(AccionHistorial.REGISTRO)
            .observacion("Primer evento")
            .build();

        HistorialSolicitud historial2 = HistorialSolicitud.builder()
            .fechaHora(fecha2)
            .accion(AccionHistorial.CLASIFICACION)
            .observacion("Segundo evento")
            .build();

        HistorialSolicitud historial3 = HistorialSolicitud.builder()
            .fechaHora(fecha3)
            .accion(AccionHistorial.CIERRE)
            .observacion("Tercer evento")
            .build();

        List<HistorialSolicitud> eventos = List.of(historial1, historial2, historial3);
        when(historialRepository.findBySolicitudIdOrderByFechaHoraAsc(solicitudId)).thenReturn(eventos);

        // Act
        List<HistorialEntryDTO> resultado = historialService.listarPorSolicitud(solicitudId);

        // Assert
        assertEquals(3, resultado.size());
        assertEquals(fecha1, resultado.get(0).fechaHora());
        assertEquals(fecha2, resultado.get(1).fechaHora());
        assertEquals(fecha3, resultado.get(2).fechaHora());

        // Verify interactions
        verify(historialRepository, times(1)).findBySolicitudIdOrderByFechaHoraAsc(solicitudId);
    }
}
