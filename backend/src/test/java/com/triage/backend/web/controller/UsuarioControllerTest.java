package com.triage.backend.web.controller;

import com.triage.backend.domain.enums.RolNombre;
import com.triage.backend.service.IUsuarioService;
import com.triage.backend.web.dto.UsuarioDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de capa web para {@link UsuarioController}.
 *
 * Cubre endpoints:
 * - GET /api/v1/usuarios
 * - GET /api/v1/usuarios/{id}
 * - PATCH /api/v1/usuarios/{id}/activar
 * - PATCH /api/v1/usuarios/{id}/desactivar
 * - GET /api/v1/usuarios/responsables
 *
 * Utiliza MockMvc sin levantar toda la aplicación (sin BD, sin Spring Boot completo).
 * El servicio `IUsuarioService` es mocked manualmente.
 */
@DisplayName("UsuarioController - Pruebas Web")
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IUsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UsuarioController controller = new UsuarioController(usuarioService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ============================================================================
    // PRUEBAS: GET /api/v1/usuarios
    // ============================================================================

    @Test
    @DisplayName("GET /api/v1/usuarios: debe responder 200 OK")
    void testListarResponde200OK() throws Exception {
        // Arrange
        UsuarioDTO usuario1 = new UsuarioDTO(1L, "Juan Pérez", "juan@email.com", "1111111111", true, RolNombre.ESTUDIANTE);
        UsuarioDTO usuario2 = new UsuarioDTO(2L, "María García", "maria@email.com", "2222222222", true, RolNombre.ADMINISTRATIVO);
        List<UsuarioDTO> usuarios = List.of(usuario1, usuario2);

        when(usuarioService.listar()).thenReturn(usuarios);

        // Act & Assert
        mockMvc.perform(get("/api/v1/usuarios")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify
        verify(usuarioService, times(1)).listar();
    }

    @Test
    @DisplayName("GET /api/v1/usuarios: debe devolver correctamente una lista JSON de UsuarioDTO")
    void testListarDevuelveLista() throws Exception {
        // Arrange
        UsuarioDTO usuario1 = new UsuarioDTO(1L, "Juan Pérez", "juan@email.com", "1111111111", true, RolNombre.ESTUDIANTE);
        UsuarioDTO usuario2 = new UsuarioDTO(2L, "María García", "maria@email.com", "2222222222", false, RolNombre.COORDINADOR);
        List<UsuarioDTO> usuarios = List.of(usuario1, usuario2);

        when(usuarioService.listar()).thenReturn(usuarios);

        // Act & Assert
        mockMvc.perform(get("/api/v1/usuarios")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].nombre").value("Juan Pérez"))
            .andExpect(jsonPath("$[0].email").value("juan@email.com"))
            .andExpect(jsonPath("$[0].identificacion").value("1111111111"))
            .andExpect(jsonPath("$[0].activo").value(true))
            .andExpect(jsonPath("$[0].rol").value("ESTUDIANTE"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].nombre").value("María García"))
            .andExpect(jsonPath("$[1].email").value("maria@email.com"))
            .andExpect(jsonPath("$[1].identificacion").value("2222222222"))
            .andExpect(jsonPath("$[1].activo").value(false))
            .andExpect(jsonPath("$[1].rol").value("COORDINADOR"));
    }

    @Test
    @DisplayName("GET /api/v1/usuarios: debe invocar usuarioService.listar() una vez")
    void testListarInvocaServicio() throws Exception {
        // Arrange
        List<UsuarioDTO> usuarios = List.of();
        when(usuarioService.listar()).thenReturn(usuarios);

        // Act
        mockMvc.perform(get("/api/v1/usuarios")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Verify
        verify(usuarioService, times(1)).listar();
        verifyNoMoreInteractions(usuarioService);
    }

    // ============================================================================
    // PRUEBAS: GET /api/v1/usuarios/{id}
    // ============================================================================

    @Test
    @DisplayName("GET /api/v1/usuarios/{id}: debe responder 200 OK cuando el usuario existe")
    void testDetalleResponde200OK() throws Exception {
        // Arrange
        Long id = 1L;
        UsuarioDTO usuario = new UsuarioDTO(id, "Juan Pérez", "juan@email.com", "1111111111", true, RolNombre.ESTUDIANTE);

        when(usuarioService.detalle(id)).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(get("/api/v1/usuarios/{id}", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify
        verify(usuarioService, times(1)).detalle(id);
    }

    @Test
    @DisplayName("GET /api/v1/usuarios/{id}: debe devolver correctamente el JSON del UsuarioDTO")
    void testDetalleDevuelveUsuarioDTO() throws Exception {
        // Arrange
        Long id = 2L;
        UsuarioDTO usuario = new UsuarioDTO(id, "María García", "maria@email.com", "2222222222", true, RolNombre.ADMINISTRATIVO);

        when(usuarioService.detalle(id)).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(get("/api/v1/usuarios/{id}", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.nombre").value("María García"))
            .andExpect(jsonPath("$.email").value("maria@email.com"))
            .andExpect(jsonPath("$.identificacion").value("2222222222"))
            .andExpect(jsonPath("$.activo").value(true))
            .andExpect(jsonPath("$.rol").value("ADMINISTRATIVO"));
    }

    @Test
    @DisplayName("GET /api/v1/usuarios/{id}: debe invocar usuarioService.detalle(id) con el id correcto")
    void testDetalleInvocaServicio() throws Exception {
        // Arrange
        Long id = 3L;
        UsuarioDTO usuario = new UsuarioDTO(id, "Carlos López", "carlos@email.com", "3333333333", true, RolNombre.COORDINADOR);

        when(usuarioService.detalle(id)).thenReturn(usuario);

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

        // Act
        mockMvc.perform(get("/api/v1/usuarios/{id}", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Verify
        verify(usuarioService, times(1)).detalle(captor.capture());
        verifyNoMoreInteractions(usuarioService);

        Long capturedId = captor.getValue();
        assertEquals(3L, capturedId);
    }

    // ============================================================================
    // PRUEBAS: PATCH /api/v1/usuarios/{id}/activar
    // ============================================================================

    @Test
    @DisplayName("PATCH /api/v1/usuarios/{id}/activar: debe responder 204 No Content")
    void testActivarResponde204NoContent() throws Exception {
        // Arrange
        Long id = 1L;
        doNothing().when(usuarioService).activar(id);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/usuarios/{id}/activar", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        // Verify
        verify(usuarioService, times(1)).activar(id);
        verifyNoMoreInteractions(usuarioService);
    }

    @Test
    @DisplayName("PATCH /api/v1/usuarios/{id}/activar: debe invocar usuarioService.activar(id) con el id correcto")
    void testActivarInvocaServicio() throws Exception {
        // Arrange
        Long id = 2L;
        doNothing().when(usuarioService).activar(id);

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

        // Act
        mockMvc.perform(patch("/api/v1/usuarios/{id}/activar", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Verify
        verify(usuarioService, times(1)).activar(captor.capture());
        verifyNoMoreInteractions(usuarioService);

        Long capturedId = captor.getValue();
        assertEquals(2L, capturedId);
    }

    // ============================================================================
    // PRUEBAS: PATCH /api/v1/usuarios/{id}/desactivar
    // ============================================================================

    @Test
    @DisplayName("PATCH /api/v1/usuarios/{id}/desactivar: debe responder 204 No Content")
    void testDesactivarResponde204NoContent() throws Exception {
        // Arrange
        Long id = 1L;
        doNothing().when(usuarioService).desactivar(id);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/usuarios/{id}/desactivar", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        // Verify
        verify(usuarioService, times(1)).desactivar(id);
        verifyNoMoreInteractions(usuarioService);
    }

    @Test
    @DisplayName("PATCH /api/v1/usuarios/{id}/desactivar: debe invocar usuarioService.desactivar(id) con el id correcto")
    void testDesactivarInvocaServicio() throws Exception {
        // Arrange
        Long id = 2L;
        doNothing().when(usuarioService).desactivar(id);

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

        // Act
        mockMvc.perform(patch("/api/v1/usuarios/{id}/desactivar", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Verify
        verify(usuarioService, times(1)).desactivar(captor.capture());
        verifyNoMoreInteractions(usuarioService);

        Long capturedId = captor.getValue();
        assertEquals(2L, capturedId);
    }

    // ============================================================================
    // PRUEBAS: GET /api/v1/usuarios/responsables
    // ============================================================================

    @Test
    @DisplayName("GET /api/v1/usuarios/responsables: debe responder 200 OK")
    void testListarResponsablesResponde200OK() throws Exception {
        // Arrange
        UsuarioDTO admin = new UsuarioDTO(1L, "Admin User", "admin@email.com", "1111111111", true, RolNombre.ADMINISTRATIVO);
        UsuarioDTO coordinador = new UsuarioDTO(2L, "Coordinador User", "coordinador@email.com", "2222222222", true, RolNombre.COORDINADOR);
        List<UsuarioDTO> responsables = List.of(admin, coordinador);

        when(usuarioService.listarResponsablesActivos()).thenReturn(responsables);

        // Act & Assert
        mockMvc.perform(get("/api/v1/usuarios/responsables")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify
        verify(usuarioService, times(1)).listarResponsablesActivos();
    }

    @Test
    @DisplayName("GET /api/v1/usuarios/responsables: debe devolver correctamente una lista JSON de UsuarioDTO")
    void testListarResponsablesDevuelveLista() throws Exception {
        // Arrange
        UsuarioDTO admin = new UsuarioDTO(1L, "Admin User", "admin@email.com", "1111111111", true, RolNombre.ADMINISTRATIVO);
        UsuarioDTO coordinador = new UsuarioDTO(2L, "Coordinador User", "coordinador@email.com", "2222222222", true, RolNombre.COORDINADOR);
        List<UsuarioDTO> responsables = List.of(admin, coordinador);

        when(usuarioService.listarResponsablesActivos()).thenReturn(responsables);

        // Act & Assert
        mockMvc.perform(get("/api/v1/usuarios/responsables")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].nombre").value("Admin User"))
            .andExpect(jsonPath("$[0].email").value("admin@email.com"))
            .andExpect(jsonPath("$[0].identificacion").value("1111111111"))
            .andExpect(jsonPath("$[0].activo").value(true))
            .andExpect(jsonPath("$[0].rol").value("ADMINISTRATIVO"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].nombre").value("Coordinador User"))
            .andExpect(jsonPath("$[1].email").value("coordinador@email.com"))
            .andExpect(jsonPath("$[1].identificacion").value("2222222222"))
            .andExpect(jsonPath("$[1].activo").value(true))
            .andExpect(jsonPath("$[1].rol").value("COORDINADOR"));
    }

    @Test
    @DisplayName("GET /api/v1/usuarios/responsables: debe invocar usuarioService.listarResponsablesActivos() una vez")
    void testListarResponsablesInvocaServicio() throws Exception {
        // Arrange
        List<UsuarioDTO> responsables = List.of();
        when(usuarioService.listarResponsablesActivos()).thenReturn(responsables);

        // Act
        mockMvc.perform(get("/api/v1/usuarios/responsables")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Verify
        verify(usuarioService, times(1)).listarResponsablesActivos();
        verifyNoMoreInteractions(usuarioService);
    }
}
