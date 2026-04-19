package com.triage.backend.service.impl;

import com.triage.backend.domain.entity.Usuario;
import com.triage.backend.domain.enums.RolNombre;
import com.triage.backend.exception.NotFoundException;
import com.triage.backend.repository.UsuarioRepository;
import com.triage.backend.web.dto.UsuarioDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link UsuarioServiceImpl}.
 *
 * Cubre métodos `listar()`, `detalle()`, `activar()`, `desactivar()` y
 * `listarResponsablesActivos()` sin usar base de datos, Spring completo ni
 * mocks innecesarios. Usa Mockito para todas las dependencias.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioServiceImpl - Pruebas Unitarias")
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl(usuarioRepository);
    }

    // ============================================================================
    // PRUEBAS: listar()
    // ============================================================================

    @Test
    @DisplayName("listar: debe devolver correctamente una lista de UsuarioDTO")
    void testListarRetornaListaUsuariosDTO() {
        // Arrange
        Usuario usuario1 = Usuario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@example.com")
            .identificacion("1234567890")
            .activo(true)
            .rol(RolNombre.ESTUDIANTE)
            .build();

        Usuario usuario2 = Usuario.builder()
            .id(2L)
            .nombre("María García")
            .email("maria@example.com")
            .identificacion("0987654321")
            .activo(true)
            .rol(RolNombre.ADMINISTRATIVO)
            .build();

        List<Usuario> usuarios = List.of(usuario1, usuario2);
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        // Act
        List<UsuarioDTO> resultado = usuarioService.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).nombre());
        assertEquals("juan@example.com", resultado.get(0).email());
        assertEquals("María García", resultado.get(1).nombre());
        assertEquals("maria@example.com", resultado.get(1).email());

        // Verify interactions
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("listar: debe devolver una lista vacía cuando el repositorio retorna vacío")
    void testListarRetornaListaVacia() {
        // Arrange
        when(usuarioRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<UsuarioDTO> resultado = usuarioService.listar();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        assertEquals(0, resultado.size());

        // Verify interactions
        verify(usuarioRepository, times(1)).findAll();
        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("listar: debe mapear correctamente los campos de Usuario a UsuarioDTO")
    void testListarMapeaCamposCorrectamente() {
        // Arrange
        Usuario usuario = Usuario.builder()
            .id(99L)
            .nombre("Carlos López")
            .email("carlos@example.com")
            .identificacion("5555555555")
            .activo(false)
            .rol(RolNombre.COORDINADOR)
            .build();

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        // Act
        List<UsuarioDTO> resultado = usuarioService.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        UsuarioDTO dto = resultado.get(0);
        assertEquals(99L, dto.id());
        assertEquals("Carlos López", dto.nombre());
        assertEquals("carlos@example.com", dto.email());
        assertEquals("5555555555", dto.identificacion());
        assertFalse(dto.activo());
        assertEquals(RolNombre.COORDINADOR, dto.rol());
    }

    // ============================================================================
    // PRUEBAS: detalle(Long id)
    // ============================================================================

    @Test
    @DisplayName("detalle: debe devolver correctamente el UsuarioDTO cuando el usuario existe")
    void testDetalleRetornaUsuarioDTOExistente() {
        // Arrange
        Long id = 1L;
        Usuario usuario = Usuario.builder()
            .id(id)
            .nombre("Juan Pérez")
            .email("juan@example.com")
            .identificacion("1234567890")
            .activo(true)
            .rol(RolNombre.ESTUDIANTE)
            .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        // Act
        UsuarioDTO resultado = usuarioService.detalle(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Juan Pérez", resultado.nombre());
        assertEquals("juan@example.com", resultado.email());
        assertEquals("1234567890", resultado.identificacion());
        assertTrue(resultado.activo());
        assertEquals(RolNombre.ESTUDIANTE, resultado.rol());

        // Verify interactions
        verify(usuarioRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("detalle: debe lanzar NotFoundException cuando el usuario no existe")
    void testDetalleUsuarioNoExiste() {
        // Arrange
        Long id = 999L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            usuarioService.detalle(id);
        });

        assertEquals("Usuario no encontrado con id: " + id, exception.getMessage());

        // Verify interactions
        verify(usuarioRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("detalle: debe usar el mensaje real del proyecto: 'Usuario no encontrado con id: <id>'")
    void testDetalleMensajeExcepcionCorrecto() {
        // Arrange
        Long id = 123L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            usuarioService.detalle(id);
        });

        String expectedMessage = "Usuario no encontrado con id: 123";
        assertEquals(expectedMessage, exception.getMessage());
    }

    // ============================================================================
    // PRUEBAS: activar(Long id)
    // ============================================================================

    @Test
    @DisplayName("activar: debe activar correctamente un usuario existente")
    void testActivarUsuarioExistente() {
        // Arrange
        Long id = 1L;
        Usuario usuario = Usuario.builder()
            .id(id)
            .nombre("Juan Pérez")
            .email("juan@example.com")
            .identificacion("1234567890")
            .activo(false)  // Inicialmente inactivo
            .rol(RolNombre.ESTUDIANTE)
            .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        // Act
        usuarioService.activar(id);

        // Assert
        assertTrue(usuario.isActivo());

        // Verify interactions
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("activar: debe guardar el usuario actualizado en el repositorio")
    void testActivarGuardaUsuario() {
        // Arrange
        Long id = 2L;
        Usuario usuario = Usuario.builder()
            .id(id)
            .nombre("María García")
            .email("maria@example.com")
            .identificacion("0987654321")
            .activo(false)
            .rol(RolNombre.ADMINISTRATIVO)
            .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        // Act
        usuarioService.activar(id);

        // Assert & Verify
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).save(argThat(u -> u.isActivo() && u.getId().equals(id)));
    }

    @Test
    @DisplayName("activar: debe lanzar NotFoundException cuando el usuario no existe")
    void testActivarUsuarioNoExiste() {
        // Arrange
        Long id = 999L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            usuarioService.activar(id);
        });

        assertEquals("Usuario no encontrado con id: " + id, exception.getMessage());

        // Verify that findById was called and save was NOT called
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("activar: debe usar el mensaje real del proyecto: 'Usuario no encontrado con id: <id>'")
    void testActivarMensajeExcepcionCorrecto() {
        // Arrange
        Long id = 456L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            usuarioService.activar(id);
        });

        String expectedMessage = "Usuario no encontrado con id: 456";
        assertEquals(expectedMessage, exception.getMessage());
    }

    // ============================================================================
    // PRUEBAS: desactivar(Long id)
    // ============================================================================

    @Test
    @DisplayName("desactivar: debe desactivar correctamente un usuario existente")
    void testDesactivarUsuarioExistente() {
        // Arrange
        Long id = 1L;
        Usuario usuario = Usuario.builder()
            .id(id)
            .nombre("Juan Pérez")
            .email("juan@example.com")
            .identificacion("1234567890")
            .activo(true)  // Inicialmente activo
            .rol(RolNombre.ESTUDIANTE)
            .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        // Act
        usuarioService.desactivar(id);

        // Assert
        assertFalse(usuario.isActivo());

        // Verify interactions
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("desactivar: debe guardar el usuario actualizado en el repositorio")
    void testDesactivarGuardaUsuario() {
        // Arrange
        Long id = 2L;
        Usuario usuario = Usuario.builder()
            .id(id)
            .nombre("María García")
            .email("maria@example.com")
            .identificacion("0987654321")
            .activo(true)
            .rol(RolNombre.ADMINISTRATIVO)
            .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        // Act
        usuarioService.desactivar(id);

        // Assert & Verify
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).save(argThat(u -> !u.isActivo() && u.getId().equals(id)));
    }

    @Test
    @DisplayName("desactivar: debe lanzar NotFoundException cuando el usuario no existe")
    void testDesactivarUsuarioNoExiste() {
        // Arrange
        Long id = 999L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            usuarioService.desactivar(id);
        });

        assertEquals("Usuario no encontrado con id: " + id, exception.getMessage());

        // Verify that findById was called and save was NOT called
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("desactivar: debe usar el mensaje real del proyecto: 'Usuario no encontrado con id: <id>'")
    void testDesactivarMensajeExcepcionCorrecto() {
        // Arrange
        Long id = 789L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            usuarioService.desactivar(id);
        });

        String expectedMessage = "Usuario no encontrado con id: 789";
        assertEquals(expectedMessage, exception.getMessage());
    }

    // ============================================================================
    // PRUEBAS: listarResponsablesActivos()
    // ============================================================================

    @Test
    @DisplayName("listarResponsablesActivos: debe devolver correctamente la lista de UsuarioDTO activos")
    void testListarResponsablesActivosRetornaListaDTO() {
        // Arrange
        Usuario usuario1 = Usuario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@example.com")
            .identificacion("1234567890")
            .activo(true)
            .rol(RolNombre.COORDINADOR)
            .build();

        Usuario usuario2 = Usuario.builder()
            .id(2L)
            .nombre("María García")
            .email("maria@example.com")
            .identificacion("0987654321")
            .activo(true)
            .rol(RolNombre.ADMINISTRATIVO)
            .build();

        List<Usuario> usuariosActivos = List.of(usuario1, usuario2);
        when(usuarioRepository.findByActivoTrue()).thenReturn(usuariosActivos);

        // Act
        List<UsuarioDTO> resultado = usuarioService.listarResponsablesActivos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).nombre());
        assertEquals("María García", resultado.get(1).nombre());
        assertTrue(resultado.get(0).activo());
        assertTrue(resultado.get(1).activo());

        // Verify interactions
        verify(usuarioRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("listarResponsablesActivos: debe devolver una lista vacía si el repositorio retorna vacío")
    void testListarResponsablesActivosRetornaListaVacia() {
        // Arrange
        when(usuarioRepository.findByActivoTrue()).thenReturn(new ArrayList<>());

        // Act
        List<UsuarioDTO> resultado = usuarioService.listarResponsablesActivos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        assertEquals(0, resultado.size());

        // Verify interactions
        verify(usuarioRepository, times(1)).findByActivoTrue();
        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("listarResponsablesActivos: debe mapear correctamente los campos al DTO")
    void testListarResponsablesActivosMapeaCamposCorrectamente() {
        // Arrange
        Usuario usuario = Usuario.builder()
            .id(77L)
            .nombre("Carlos López")
            .email("carlos@example.com")
            .identificacion("7777777777")
            .activo(true)
            .rol(RolNombre.COORDINADOR)
            .build();

        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(usuario));

        // Act
        List<UsuarioDTO> resultado = usuarioService.listarResponsablesActivos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        UsuarioDTO dto = resultado.get(0);
        assertEquals(77L, dto.id());
        assertEquals("Carlos López", dto.nombre());
        assertEquals("carlos@example.com", dto.email());
        assertEquals("7777777777", dto.identificacion());
        assertTrue(dto.activo());
        assertEquals(RolNombre.COORDINADOR, dto.rol());
    }

    @Test
    @DisplayName("listarResponsablesActivos: debe usar findByActivoTrue() del repositorio")
    void testListarResponsablesActivosUsaFindByActivoTrue() {
        // Arrange
        Usuario usuario = Usuario.builder()
            .id(1L)
            .nombre("Test User")
            .email("test@example.com")
            .identificacion("1111111111")
            .activo(true)
            .rol(RolNombre.ADMINISTRATIVO)
            .build();

        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(usuario));

        // Act
        usuarioService.listarResponsablesActivos();

        // Assert & Verify
        verify(usuarioRepository, times(1)).findByActivoTrue();
    }
}
