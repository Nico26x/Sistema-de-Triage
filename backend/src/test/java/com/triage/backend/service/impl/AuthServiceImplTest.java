package com.triage.backend.service.impl;

import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.RolNombre;
import com.triage.backend.exception.BusinessRuleException;
import com.triage.backend.exception.NotFoundException;
import com.triage.backend.repository.UsuarioRepository;
import com.triage.backend.web.dto.AuthRequestDTO;
import com.triage.backend.web.dto.RegisterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link AuthServiceImpl}.
 *
 * Cubre métodos `registrar(...)` y `autenticar(...)` sin usar base de datos,
 * Spring completo ni mocks innecesarios. Usa Mockito para todas las dependencias.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl - Pruebas Unitarias")
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(usuarioRepository, passwordEncoder);
    }

    // ============================================================================
    // PRUEBAS: registrar(...)
    // ============================================================================

    @Test
    @DisplayName("registrar: debe registrar correctamente un usuario válido")
    void testRegistrarUsuarioValido() {
        // Arrange
        RegisterRequestDTO req = new RegisterRequestDTO(
            "Juan Pérez",
            "juan@example.com",
            "1234567890",
            "password123",
            RolNombre.ESTUDIANTE
        );

        String passwordCodificada = "hashPasswordCodec123";

        when(usuarioRepository.findByEmail(req.email())).thenReturn(Optional.empty());
        when(usuarioRepository.findByIdentificacion(req.identificacion())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(req.password())).thenReturn(passwordCodificada);

        Usuario usuarioGuardado = Usuario.builder()
            .id(1L)
            .nombre(req.nombre())
            .email(req.email())
            .identificacion(req.identificacion())
            .passwordHash(passwordCodificada)
            .activo(true)
            .rol(req.rol())
            .build();

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // Act
        Usuario resultado = authService.registrar(req);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan Pérez", resultado.getNombre());
        assertEquals("juan@example.com", resultado.getEmail());
        assertEquals("1234567890", resultado.getIdentificacion());
        assertEquals(passwordCodificada, resultado.getPasswordHash());
        assertTrue(resultado.isActivo());
        assertEquals(RolNombre.ESTUDIANTE, resultado.getRol());

        // Verify interactions
        verify(usuarioRepository, times(1)).findByEmail(req.email());
        verify(usuarioRepository, times(1)).findByIdentificacion(req.identificacion());
        verify(passwordEncoder, times(1)).encode(req.password());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("registrar: debe lanzar BusinessRuleException si el email ya existe")
    void testRegistrarEmailYaExiste() {
        // Arrange
        RegisterRequestDTO req = new RegisterRequestDTO(
            "Juan Pérez",
            "juan@example.com",
            "1234567890",
            "password123",
            RolNombre.ESTUDIANTE
        );

        Usuario usuarioExistente = Usuario.builder()
            .id(999L)
            .nombre("Otro Usuario")
            .email(req.email())
            .identificacion("9876543210")
            .passwordHash("otroHash")
            .activo(true)
            .rol(RolNombre.ADMINISTRATIVO)
            .build();

        when(usuarioRepository.findByEmail(req.email())).thenReturn(Optional.of(usuarioExistente));

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            authService.registrar(req);
        });

        assertEquals("El email ya está registrado", exception.getMessage());

        // Verify that save was NOT called, encode was NOT called, and findByIdentificacion was NOT called
        // (flow should stop at email validation)
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(usuarioRepository, never()).findByIdentificacion(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("registrar: debe lanzar BusinessRuleException si la identificación ya existe")
    void testRegistrarIdentificacionYaExiste() {
        // Arrange
        RegisterRequestDTO req = new RegisterRequestDTO(
            "Juan Pérez",
            "juan@example.com",
            "1234567890",
            "password123",
            RolNombre.ESTUDIANTE
        );

        Usuario usuarioExistente = Usuario.builder()
            .id(999L)
            .nombre("Otro Usuario")
            .email("otro@example.com")
            .identificacion(req.identificacion())
            .passwordHash("otroHash")
            .activo(true)
            .rol(RolNombre.ADMINISTRATIVO)
            .build();

        when(usuarioRepository.findByEmail(req.email())).thenReturn(Optional.empty());
        when(usuarioRepository.findByIdentificacion(req.identificacion())).thenReturn(Optional.of(usuarioExistente));

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            authService.registrar(req);
        });

        assertEquals("La identificación ya está registrada", exception.getMessage());

        // Verify that save was NOT called
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("registrar: debe codificar la contraseña usando passwordEncoder.encode()")
    void testRegistrarCodificaContraseña() {
        // Arrange
        RegisterRequestDTO req = new RegisterRequestDTO(
            "Juan Pérez",
            "juan@example.com",
            "1234567890",
            "miContraseña123",
            RolNombre.COORDINADOR
        );

        String passwordCodificada = "$2a$10$encrypted.password.hash";

        when(usuarioRepository.findByEmail(req.email())).thenReturn(Optional.empty());
        when(usuarioRepository.findByIdentificacion(req.identificacion())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("miContraseña123")).thenReturn(passwordCodificada);

        Usuario usuarioGuardado = Usuario.builder()
            .id(1L)
            .nombre(req.nombre())
            .email(req.email())
            .identificacion(req.identificacion())
            .passwordHash(passwordCodificada)
            .activo(true)
            .rol(req.rol())
            .build();

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // Act
        authService.registrar(req);

        // Assert & Verify
        verify(passwordEncoder, times(1)).encode("miContraseña123");
    }

    @Test
    @DisplayName("registrar: debe guardar el usuario con activo=true")
    void testRegistrarGuardaActivoTrue() {
        // Arrange
        RegisterRequestDTO req = new RegisterRequestDTO(
            "María García",
            "maria@example.com",
            "0987654321",
            "password456",
            RolNombre.ADMINISTRATIVO
        );

        String passwordCodificada = "encodedPassword456";

        when(usuarioRepository.findByEmail(req.email())).thenReturn(Optional.empty());
        when(usuarioRepository.findByIdentificacion(req.identificacion())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(req.password())).thenReturn(passwordCodificada);

        Usuario usuarioGuardado = Usuario.builder()
            .id(2L)
            .nombre(req.nombre())
            .email(req.email())
            .identificacion(req.identificacion())
            .passwordHash(passwordCodificada)
            .activo(true)
            .rol(req.rol())
            .build();

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // Act
        Usuario resultado = authService.registrar(req);

        // Assert
        assertTrue(resultado.isActivo());
        verify(usuarioRepository, times(1)).save(argThat(usuario ->
            usuario.isActivo() &&
            usuario.getRol().equals(RolNombre.ADMINISTRATIVO) &&
            usuario.getPasswordHash().equals(passwordCodificada)
        ));
    }

    @Test
    @DisplayName("registrar: debe devolver el Usuario guardado")
    void testRegistrarDevuelveUsuarioGuardado() {
        // Arrange
        RegisterRequestDTO req = new RegisterRequestDTO(
            "Carlos López",
            "carlos@example.com",
            "5555555555",
            "password789",
            RolNombre.ESTUDIANTE
        );

        String passwordCodificada = "hashCarlos789";

        when(usuarioRepository.findByEmail(req.email())).thenReturn(Optional.empty());
        when(usuarioRepository.findByIdentificacion(req.identificacion())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(req.password())).thenReturn(passwordCodificada);

        Usuario usuarioGuardado = Usuario.builder()
            .id(5L)
            .nombre("Carlos López")
            .email("carlos@example.com")
            .identificacion("5555555555")
            .passwordHash(passwordCodificada)
            .activo(true)
            .rol(RolNombre.ESTUDIANTE)
            .build();

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // Act
        Usuario resultado = authService.registrar(req);

        // Assert
        assertNotNull(resultado);
        assertEquals(5L, resultado.getId());
        assertEquals("Carlos López", resultado.getNombre());
        assertEquals("carlos@example.com", resultado.getEmail());
    }

    // ============================================================================
    // PRUEBAS: autenticar(...)
    // ============================================================================

    @Test
    @DisplayName("autenticar: debe autenticar correctamente cuando datos son válidos")
    void testAutenticarUsuarioValido() {
        // Arrange
        AuthRequestDTO req = new AuthRequestDTO("juan@example.com", "password123");

        Usuario usuarioEnBD = Usuario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@example.com")
            .identificacion("1234567890")
            .passwordHash("$2a$10$hashedPassword123")
            .activo(true)
            .rol(RolNombre.ESTUDIANTE)
            .build();

        when(usuarioRepository.findByEmail(req.email())).thenReturn(Optional.of(usuarioEnBD));
        when(passwordEncoder.matches(req.password(), usuarioEnBD.getPasswordHash())).thenReturn(true);

        // Act
        Usuario resultado = authService.autenticar(req);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("juan@example.com", resultado.getEmail());
        assertTrue(resultado.isActivo());
        assertEquals(RolNombre.ESTUDIANTE, resultado.getRol());

        // Verify interactions
        verify(usuarioRepository, times(1)).findByEmail(req.email());
        verify(passwordEncoder, times(1)).matches(req.password(), usuarioEnBD.getPasswordHash());
    }

    @Test
    @DisplayName("autenticar: debe lanzar NotFoundException si el usuario no existe")
    void testAutenticarUsuarioNoExiste() {
        // Arrange
        AuthRequestDTO req = new AuthRequestDTO("noexiste@example.com", "password123");

        when(usuarioRepository.findByEmail(req.email())).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            authService.autenticar(req);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());

        // Verify that findByEmail was called exactly once and passwordEncoder.matches was NOT called
        verify(usuarioRepository, times(1)).findByEmail(req.email());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("autenticar: debe lanzar BusinessRuleException si la contraseña no coincide")
    void testAutenticarContraseñaIncorrecta() {
        // Arrange
        AuthRequestDTO req = new AuthRequestDTO("juan@example.com", "passwordIncorrecto");

        Usuario usuarioEnBD = Usuario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@example.com")
            .identificacion("1234567890")
            .passwordHash("$2a$10$hashedPassword123")
            .activo(true)
            .rol(RolNombre.ESTUDIANTE)
            .build();

        when(usuarioRepository.findByEmail(req.email())).thenReturn(Optional.of(usuarioEnBD));
        when(passwordEncoder.matches("passwordIncorrecto", usuarioEnBD.getPasswordHash())).thenReturn(false);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            authService.autenticar(req);
        });

        assertEquals("Contraseña incorrecta", exception.getMessage());

        // Verify that findByEmail was called but usuario was not processed further
        verify(usuarioRepository, times(1)).findByEmail(req.email());
        verify(passwordEncoder, times(1)).matches(req.password(), usuarioEnBD.getPasswordHash());
    }

    @Test
    @DisplayName("autenticar: debe lanzar BusinessRuleException si el usuario está inactivo")
    void testAutenticarUsuarioInactivo() {
        // Arrange
        AuthRequestDTO req = new AuthRequestDTO("juan@example.com", "password123");

        Usuario usuarioEnBD = Usuario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@example.com")
            .identificacion("1234567890")
            .passwordHash("$2a$10$hashedPassword123")
            .activo(false)
            .rol(RolNombre.ESTUDIANTE)
            .build();

        when(usuarioRepository.findByEmail(req.email())).thenReturn(Optional.of(usuarioEnBD));
        when(passwordEncoder.matches(req.password(), usuarioEnBD.getPasswordHash())).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            authService.autenticar(req);
        });

        assertEquals("El usuario está inactivo", exception.getMessage());

        // Verify that passwordEncoder.matches was called but after that the validation stops
        verify(usuarioRepository, times(1)).findByEmail(req.email());
        verify(passwordEncoder, times(1)).matches(req.password(), usuarioEnBD.getPasswordHash());
    }

    @Test
    @DisplayName("autenticar: debe usar passwordEncoder.matches() para validar la contraseña")
    void testAutenticarUsaPasswordEncoderMatches() {
        // Arrange
        AuthRequestDTO req = new AuthRequestDTO("test@example.com", "inputPassword");

        Usuario usuarioEnBD = Usuario.builder()
            .id(1L)
            .nombre("Test User")
            .email("test@example.com")
            .identificacion("1111111111")
            .passwordHash("$2a$10$hashedPasswordInDB")
            .activo(true)
            .rol(RolNombre.ADMINISTRATIVO)
            .build();

        when(usuarioRepository.findByEmail(req.email())).thenReturn(Optional.of(usuarioEnBD));
        when(passwordEncoder.matches("inputPassword", "$2a$10$hashedPasswordInDB")).thenReturn(true);

        // Act
        authService.autenticar(req);

        // Assert & Verify
        verify(passwordEncoder, times(1)).matches("inputPassword", "$2a$10$hashedPasswordInDB");
    }

    @Test
    @DisplayName("autenticar: debe validar contraseña antes de verificar que usuario está activo")
    void testAutenticarValidaContraseñaAntesDeActivo() {
        // Arrange
        AuthRequestDTO req = new AuthRequestDTO("usuario@example.com", "wrongPassword");

        Usuario usuarioEnBD = Usuario.builder()
            .id(1L)
            .nombre("Usuario Test")
            .email("usuario@example.com")
            .identificacion("2222222222")
            .passwordHash("$2a$10$correctHash")
            .activo(false)
            .rol(RolNombre.COORDINADOR)
            .build();

        when(usuarioRepository.findByEmail(req.email())).thenReturn(Optional.of(usuarioEnBD));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$correctHash")).thenReturn(false);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            authService.autenticar(req);
        });

        // Debe lanzar excepción por contraseña incorrecta, antes de verificar estado activo
        assertEquals("Contraseña incorrecta", exception.getMessage());
    }

    @Test
    @DisplayName("autenticar: debe retornar el usuario cuando todas las validaciones son exitosas")
    void testAutenticarRetornaUsuario() {
        // Arrange
        AuthRequestDTO req = new AuthRequestDTO("admin@example.com", "adminPassword");

        Usuario usuarioEnBD = Usuario.builder()
            .id(10L)
            .nombre("Admin User")
            .email("admin@example.com")
            .identificacion("9999999999")
            .passwordHash("$2a$10$adminHash")
            .activo(true)
            .rol(RolNombre.ADMINISTRATIVO)
            .build();

        when(usuarioRepository.findByEmail(req.email())).thenReturn(Optional.of(usuarioEnBD));
        when(passwordEncoder.matches(req.password(), usuarioEnBD.getPasswordHash())).thenReturn(true);

        // Act
        Usuario resultado = authService.autenticar(req);

        // Assert
        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals("Admin User", resultado.getNombre());
        assertEquals("admin@example.com", resultado.getEmail());
        assertEquals(RolNombre.ADMINISTRATIVO, resultado.getRol());
    }
}
