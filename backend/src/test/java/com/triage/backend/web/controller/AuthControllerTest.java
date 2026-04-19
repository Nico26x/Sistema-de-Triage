package com.triage.backend.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.RolNombre;
import com.triage.backend.service.IAuthService;
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

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de capa web para {@link AuthController}.
 *
 * Cubre endpoints:
 * - POST /api/v1/auth/register
 * - POST /api/v1/auth/login
 *
 * Utiliza MockMvc sin levantar toda la aplicación (sin BD, sin Spring Boot completo).
 * El servicio `IAuthService` es mocked manualmente.
 */
@DisplayName("AuthController - Pruebas Web")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ============================================================================
    // PRUEBAS: POST /api/v1/auth/register
    // ============================================================================

    @Test
    @DisplayName("POST /api/v1/auth/register: debe responder 201 Created cuando el request es válido")
    void testRegistrarResponde201Created() throws Exception {
        // Arrange
        RegisterRequestDTO req = new RegisterRequestDTO(
            "Juan Pérez",
            "juan.perez@email.com",
            "1234567890",
            "SecurePassword123!",
            RolNombre.ESTUDIANTE
        );

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan Pérez");
        usuario.setEmail("juan.perez@email.com");
        usuario.setIdentificacion("1234567890");
        usuario.setActivo(true);
        usuario.setRol(RolNombre.ESTUDIANTE);

        when(authService.registrar(req)).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify
        verify(authService, times(1)).registrar(any());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register: debe devolver correctamente el JSON del UsuarioDTO")
    void testRegistrarDevuelveUsuarioDTO() throws Exception {
        // Arrange
        RegisterRequestDTO req = new RegisterRequestDTO(
            "María García",
            "maria.garcia@email.com",
            "9876543210",
            "AnotherPassword456!",
            RolNombre.ADMINISTRATIVO
        );

        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setNombre("María García");
        usuario.setEmail("maria.garcia@email.com");
        usuario.setIdentificacion("9876543210");
        usuario.setActivo(true);
        usuario.setRol(RolNombre.ADMINISTRATIVO);

        when(authService.registrar(req)).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.nombre").value("María García"))
            .andExpect(jsonPath("$.email").value("maria.garcia@email.com"))
            .andExpect(jsonPath("$.identificacion").value("9876543210"))
            .andExpect(jsonPath("$.activo").value(true))
            .andExpect(jsonPath("$.rol").value("ADMINISTRATIVO"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register: debe invocar authService.registrar(req) con el RegisterRequestDTO correcto")
    void testRegistrarInvocaServicio() throws Exception {
        // Arrange
        RegisterRequestDTO req = new RegisterRequestDTO(
            "Carlos López",
            "carlos.lopez@email.com",
            "5555555555",
            "Password789!",
            RolNombre.COORDINADOR
        );

        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNombre("Carlos López");
        usuario.setEmail("carlos.lopez@email.com");
        usuario.setIdentificacion("5555555555");
        usuario.setActivo(true);
        usuario.setRol(RolNombre.COORDINADOR);

        when(authService.registrar(req)).thenReturn(usuario);

        ArgumentCaptor<RegisterRequestDTO> captor = ArgumentCaptor.forClass(RegisterRequestDTO.class);

        // Act
        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        // Assert
        verify(authService, times(1)).registrar(captor.capture());
        verifyNoMoreInteractions(authService);

        RegisterRequestDTO capturedDto = captor.getValue();
        assertEquals("Carlos López", capturedDto.nombre());
        assertEquals("carlos.lopez@email.com", capturedDto.email());
        assertEquals("5555555555", capturedDto.identificacion());
        assertEquals("Password789!", capturedDto.password());
        assertEquals(RolNombre.COORDINADOR, capturedDto.rol());
    }

    // ============================================================================
    // PRUEBAS: POST /api/v1/auth/login
    // ============================================================================

    @Test
    @DisplayName("POST /api/v1/auth/login: debe responder 200 OK cuando el request es válido")
    void testLoginResponde200OK() throws Exception {
        // Arrange
        AuthRequestDTO req = new AuthRequestDTO(
            "usuario@email.com",
            "password123"
        );

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Usuario Test");
        usuario.setEmail("usuario@email.com");
        usuario.setActivo(true);

        when(authService.autenticar(req)).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify
        verify(authService, times(1)).autenticar(any());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login: debe devolver correctamente el JSON del AuthResponseDTO")
    void testLoginDevuelveAuthResponseDTO() throws Exception {
        // Arrange
        AuthRequestDTO req = new AuthRequestDTO(
            "admin@email.com",
            "adminpass456"
        );

        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setNombre("Admin User");
        usuario.setEmail("admin@email.com");
        usuario.setActivo(true);

        when(authService.autenticar(req)).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value(startsWith("placeholder-token-")))
            .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login: debe invocar authService.autenticar(req) con el AuthRequestDTO correcto")
    void testLoginInvocaServicio() throws Exception {
        // Arrange
        AuthRequestDTO req = new AuthRequestDTO(
            "test@email.com",
            "testpass789"
        );

        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNombre("Test User");
        usuario.setEmail("test@email.com");
        usuario.setActivo(true);

        when(authService.autenticar(req)).thenReturn(usuario);

        ArgumentCaptor<AuthRequestDTO> captor = ArgumentCaptor.forClass(AuthRequestDTO.class);

        // Act
        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());

        // Assert
        verify(authService, times(1)).autenticar(captor.capture());
        verifyNoMoreInteractions(authService);

        AuthRequestDTO capturedDto = captor.getValue();
        assertEquals("test@email.com", capturedDto.email());
        assertEquals("testpass789", capturedDto.password());
    }
}
