package com.triage.backend.service.impl;

import com.triage.backend.domain.enums.EstadoSolicitud;
import com.triage.backend.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link MaquinaEstadosSolicitudImpl}.
 * 
 * Cubre la validación de transiciones de estados de solicitudes sin usar Spring,
 * base de datos ni mocks. Pruebas puras sobre la lógica de máquina de estados.
 */
@DisplayName("MaquinaEstadosSolicitudImpl - Pruebas Unitarias")
class MaquinaEstadosSolicitudImplTest {

    private MaquinaEstadosSolicitudImpl maquinaEstados;

    @BeforeEach
    void setUp() {
        // Instancia directa del servicio, sin Spring
        maquinaEstados = new MaquinaEstadosSolicitudImpl();
    }

    // ============================================================================
    // PRUEBAS: Transiciones Válidas
    // ============================================================================

    @Test
    @DisplayName("esTransicionValida: REGISTRADA -> CLASIFICADA es válida")
    void testTransicionValidaRegistradaAClasificada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.REGISTRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.CLASIFICADA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertTrue(resultado, "Transición de REGISTRADA a CLASIFICADA debe ser válida");
    }

    @Test
    @DisplayName("esTransicionValida: CLASIFICADA -> EN_ATENCION es válida")
    void testTransicionValidaClasificadaAEnAtencion() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CLASIFICADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.EN_ATENCION;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertTrue(resultado, "Transición de CLASIFICADA a EN_ATENCION debe ser válida");
    }

    @Test
    @DisplayName("esTransicionValida: EN_ATENCION -> ATENDIDA es válida")
    void testTransicionValidaEnAtencionAAtendida() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.EN_ATENCION;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.ATENDIDA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertTrue(resultado, "Transición de EN_ATENCION a ATENDIDA debe ser válida");
    }

    @Test
    @DisplayName("esTransicionValida: ATENDIDA -> CERRADA es válida")
    void testTransicionValidaAtendidaACerrada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.ATENDIDA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.CERRADA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertTrue(resultado, "Transición de ATENDIDA a CERRADA debe ser válida");
    }

    // ============================================================================
    // PRUEBAS: Transiciones Inválidas
    // ============================================================================

    @Test
    @DisplayName("esTransicionValida: REGISTRADA -> ATENDIDA es inválida")
    void testTransicionInvalidaRegistradaAAtendida() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.REGISTRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.ATENDIDA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Transición de REGISTRADA a ATENDIDA debe ser inválida");
    }

    @Test
    @DisplayName("esTransicionValida: REGISTRADA -> CERRADA es inválida")
    void testTransicionInvalidaRegistradaACerrada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.REGISTRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.CERRADA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Transición de REGISTRADA a CERRADA debe ser inválida");
    }

    @Test
    @DisplayName("esTransicionValida: REGISTRADA -> EN_ATENCION es inválida")
    void testTransicionInvalidaRegistradaAEnAtencion() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.REGISTRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.EN_ATENCION;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Transición de REGISTRADA a EN_ATENCION debe ser inválida");
    }

    @Test
    @DisplayName("esTransicionValida: CLASIFICADA -> CERRADA es inválida")
    void testTransicionInvalidaClasificadaACerrada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CLASIFICADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.CERRADA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Transición de CLASIFICADA a CERRADA debe ser inválida");
    }

    @Test
    @DisplayName("esTransicionValida: CLASIFICADA -> ATENDIDA es inválida")
    void testTransicionInvalidaClasificadaAAtendida() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CLASIFICADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.ATENDIDA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Transición de CLASIFICADA a ATENDIDA debe ser inválida");
    }

    @Test
    @DisplayName("esTransicionValida: CLASIFICADA -> REGISTRADA es inválida")
    void testTransicionInvalidaClasificadaARegistrada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CLASIFICADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.REGISTRADA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Transición de CLASIFICADA a REGISTRADA debe ser inválida (sin retrocesos)");
    }

    @Test
    @DisplayName("esTransicionValida: EN_ATENCION -> CERRADA es inválida")
    void testTransicionInvalidaEnAtencionACerrada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.EN_ATENCION;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.CERRADA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Transición de EN_ATENCION a CERRADA debe ser inválida");
    }

    @Test
    @DisplayName("esTransicionValida: EN_ATENCION -> CLASIFICADA es inválida")
    void testTransicionInvalidaEnAtencionAClasificada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.EN_ATENCION;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.CLASIFICADA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Transición de EN_ATENCION a CLASIFICADA debe ser inválida (sin retrocesos)");
    }

    @Test
    @DisplayName("esTransicionValida: ATENDIDA -> REGISTRADA es inválida")
    void testTransicionInvalidaAtendidaARegistrada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.ATENDIDA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.REGISTRADA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Transición de ATENDIDA a REGISTRADA debe ser inválida (sin retrocesos)");
    }

    @Test
    @DisplayName("esTransicionValida: ATENDIDA -> EN_ATENCION es inválida")
    void testTransicionInvalidaAtendidaAEnAtencion() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.ATENDIDA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.EN_ATENCION;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Transición de ATENDIDA a EN_ATENCION debe ser inválida (sin retrocesos)");
    }

    @Test
    @DisplayName("esTransicionValida: ATENDIDA -> CLASIFICADA es inválida")
    void testTransicionInvalidaAtendidaAClasificada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.ATENDIDA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.CLASIFICADA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Transición de ATENDIDA a CLASIFICADA debe ser inválida (sin retrocesos)");
    }

    // ============================================================================
    // PRUEBAS: Transiciones desde CERRADA
    // ============================================================================

    @Test
    @DisplayName("esTransicionValida: CERRADA -> REGISTRADA es inválida")
    void testTransicionInvalidaCerradaARegistrada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CERRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.REGISTRADA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Desde CERRADA no se permite transición a REGISTRADA");
    }

    @Test
    @DisplayName("esTransicionValida: CERRADA -> CLASIFICADA es inválida")
    void testTransicionInvalidaCerradaAClasificada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CERRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.CLASIFICADA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Desde CERRADA no se permite transición a CLASIFICADA");
    }

    @Test
    @DisplayName("esTransicionValida: CERRADA -> EN_ATENCION es inválida")
    void testTransicionInvalidaCerradaAEnAtencion() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CERRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.EN_ATENCION;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Desde CERRADA no se permite transición a EN_ATENCION");
    }

    @Test
    @DisplayName("esTransicionValida: CERRADA -> ATENDIDA es inválida")
    void testTransicionInvalidaCerradaAAtendida() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CERRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.ATENDIDA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Desde CERRADA no se permite transición a ATENDIDA");
    }

    @Test
    @DisplayName("esTransicionValida: CERRADA -> CERRADA es inválida")
    void testTransicionInvalidaCerradaACerrada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CERRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.CERRADA;

        // Act
        boolean resultado = maquinaEstados.esTransicionValida(estadoActual, nuevoEstado);

        // Assert
        assertFalse(resultado, "Desde CERRADA no se permite ni siquiera transición a sí misma");
    }

    // ============================================================================
    // PRUEBAS: validarTransicion con Transiciones Válidas (sin excepción)
    // ============================================================================

    @Test
    @DisplayName("validarTransicion: REGISTRADA -> CLASIFICADA no lanza excepción")
    void testValidarTransicionValidaRegistradaAClasificada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.REGISTRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.CLASIFICADA;

        // Act & Assert: no debe lanzar excepción
        assertDoesNotThrow(() -> 
            maquinaEstados.validarTransicion(estadoActual, nuevoEstado),
            "Transición válida no debe lanzar excepción"
        );
    }

    @Test
    @DisplayName("validarTransicion: CLASIFICADA -> EN_ATENCION no lanza excepción")
    void testValidarTransicionValidaClasificadaAEnAtencion() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CLASIFICADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.EN_ATENCION;

        // Act & Assert: no debe lanzar excepción
        assertDoesNotThrow(() -> 
            maquinaEstados.validarTransicion(estadoActual, nuevoEstado),
            "Transición válida no debe lanzar excepción"
        );
    }

    @Test
    @DisplayName("validarTransicion: EN_ATENCION -> ATENDIDA no lanza excepción")
    void testValidarTransicionValidaEnAtencionAAtendida() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.EN_ATENCION;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.ATENDIDA;

        // Act & Assert: no debe lanzar excepción
        assertDoesNotThrow(() -> 
            maquinaEstados.validarTransicion(estadoActual, nuevoEstado),
            "Transición válida no debe lanzar excepción"
        );
    }

    @Test
    @DisplayName("validarTransicion: ATENDIDA -> CERRADA no lanza excepción")
    void testValidarTransicionValidaAtendidaACerrada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.ATENDIDA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.CERRADA;

        // Act & Assert: no debe lanzar excepción
        assertDoesNotThrow(() -> 
            maquinaEstados.validarTransicion(estadoActual, nuevoEstado),
            "Transición válida no debe lanzar excepción"
        );
    }

    // ============================================================================
    // PRUEBAS: validarTransicion con Transiciones Inválidas (lanza excepción)
    // ============================================================================

    @Test
    @DisplayName("validarTransicion: REGISTRADA -> ATENDIDA lanza BusinessRuleException")
    void testValidarTransicionInvalidaRegistradaAAtendida() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.REGISTRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.ATENDIDA;

        // Act & Assert: debe lanzar excepción
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> maquinaEstados.validarTransicion(estadoActual, nuevoEstado),
            "Transición inválida debe lanzar BusinessRuleException"
        );

        // Validar que el mensaje contiene el formato esperado
        assertTrue(exception.getMessage().contains("Transición inválida"),
            "Mensaje debe contener 'Transición inválida'");
        assertTrue(exception.getMessage().contains(estadoActual.toString()),
            "Mensaje debe contener el estado actual");
        assertTrue(exception.getMessage().contains(nuevoEstado.toString()),
            "Mensaje debe contener el nuevo estado");
    }

    @Test
    @DisplayName("validarTransicion: CLASIFICADA -> CERRADA lanza BusinessRuleException")
    void testValidarTransicionInvalidaClasificadaACerrada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CLASIFICADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.CERRADA;

        // Act & Assert: debe lanzar excepción
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> maquinaEstados.validarTransicion(estadoActual, nuevoEstado),
            "Transición inválida debe lanzar BusinessRuleException"
        );

        // Validar el mensaje
        assertTrue(exception.getMessage().contains("Transición inválida"),
            "Mensaje debe contener 'Transición inválida'");
    }

    @Test
    @DisplayName("validarTransicion: ATENDIDA -> REGISTRADA lanza BusinessRuleException")
    void testValidarTransicionInvalidaAtendidaARegistrada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.ATENDIDA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.REGISTRADA;

        // Act & Assert: debe lanzar excepción
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> maquinaEstados.validarTransicion(estadoActual, nuevoEstado),
            "Transición inválida debe lanzar BusinessRuleException"
        );

        // Validar el mensaje
        assertTrue(exception.getMessage().contains("Transición inválida"),
            "Mensaje debe contener 'Transición inválida'");
    }

    @Test
    @DisplayName("validarTransicion: CERRADA -> REGISTRADA lanza BusinessRuleException")
    void testValidarTransicionInvalidaCerradaARegistrada() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CERRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.REGISTRADA;

        // Act & Assert: debe lanzar excepción
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> maquinaEstados.validarTransicion(estadoActual, nuevoEstado),
            "Desde CERRADA no se permite ninguna transición"
        );

        // Validar el mensaje
        assertTrue(exception.getMessage().contains("Transición inválida"),
            "Mensaje debe contener 'Transición inválida'");
    }

    @Test
    @DisplayName("validarTransicion: CERRADA -> ATENDIDA lanza BusinessRuleException")
    void testValidarTransicionInvalidaCerradaAAtendida() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CERRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.ATENDIDA;

        // Act & Assert: debe lanzar excepción
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> maquinaEstados.validarTransicion(estadoActual, nuevoEstado),
            "Desde CERRADA no se permite ninguna transición"
        );

        // Validar el mensaje
        assertTrue(exception.getMessage().contains("Transición inválida"),
            "Mensaje debe contener 'Transición inválida'");
    }

    // ============================================================================
    // PRUEBAS: Mensaje de Excepción Detallado
    // ============================================================================

    @Test
    @DisplayName("validarTransicion genera mensaje con formato correcto: 'Transición inválida de X a Y'")
    void testValidarTransicionMensajeFormato() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.REGISTRADA;
        EstadoSolicitud nuevoEstado = EstadoSolicitud.CERRADA;
        String mensajeEsperado = String.format("Transición inválida de %s a %s", estadoActual, nuevoEstado);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> maquinaEstados.validarTransicion(estadoActual, nuevoEstado)
        );

        // Validar que el mensaje coincida exactamente con el formato esperado
        assertEquals(mensajeEsperado, exception.getMessage(),
            "Mensaje debe estar en formato: 'Transición inválida de ESTADO1 a ESTADO2'");
    }

    // ============================================================================
    // PRUEBAS: Casos Extremos
    // ============================================================================

    @Test
    @DisplayName("esTransicionValida maneja correctamente todos los estados como origen")
    void testTransicionesDesdeTodaslosEstados() {
        // Prueba que desde cada estado solo hay una transición válida (o ninguna para CERRADA)
        
        // REGISTRADA -> solo a CLASIFICADA
        assertTrue(maquinaEstados.esTransicionValida(EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA));
        assertFalse(maquinaEstados.esTransicionValida(EstadoSolicitud.REGISTRADA, EstadoSolicitud.EN_ATENCION));
        
        // CLASIFICADA -> solo a EN_ATENCION
        assertTrue(maquinaEstados.esTransicionValida(EstadoSolicitud.CLASIFICADA, EstadoSolicitud.EN_ATENCION));
        assertFalse(maquinaEstados.esTransicionValida(EstadoSolicitud.CLASIFICADA, EstadoSolicitud.ATENDIDA));
        
        // EN_ATENCION -> solo a ATENDIDA
        assertTrue(maquinaEstados.esTransicionValida(EstadoSolicitud.EN_ATENCION, EstadoSolicitud.ATENDIDA));
        assertFalse(maquinaEstados.esTransicionValida(EstadoSolicitud.EN_ATENCION, EstadoSolicitud.CERRADA));
        
        // ATENDIDA -> solo a CERRADA
        assertTrue(maquinaEstados.esTransicionValida(EstadoSolicitud.ATENDIDA, EstadoSolicitud.CERRADA));
        assertFalse(maquinaEstados.esTransicionValida(EstadoSolicitud.ATENDIDA, EstadoSolicitud.EN_ATENCION));
        
        // CERRADA -> ninguna transición es válida
        assertFalse(maquinaEstados.esTransicionValida(EstadoSolicitud.CERRADA, EstadoSolicitud.REGISTRADA));
        assertFalse(maquinaEstados.esTransicionValida(EstadoSolicitud.CERRADA, EstadoSolicitud.CLASIFICADA));
        assertFalse(maquinaEstados.esTransicionValida(EstadoSolicitud.CERRADA, EstadoSolicitud.EN_ATENCION));
        assertFalse(maquinaEstados.esTransicionValida(EstadoSolicitud.CERRADA, EstadoSolicitud.ATENDIDA));
        assertFalse(maquinaEstados.esTransicionValida(EstadoSolicitud.CERRADA, EstadoSolicitud.CERRADA));
    }

    @Test
    @DisplayName("esTransicionValida: máquina de estados es lineal sin saltos")
    void testMaquinaEstadosEsLineal() {
        // Valida que la máquina de estados es lineal: no hay saltos sin pasar por estados intermedios
        // Por ejemplo: REGISTRADA debe pasar por CLASIFICADA antes de llegar a EN_ATENCION
        
        assertFalse(maquinaEstados.esTransicionValida(EstadoSolicitud.REGISTRADA, EstadoSolicitud.EN_ATENCION),
            "No se puede saltar de REGISTRADA a EN_ATENCION directamente");
        
        assertFalse(maquinaEstados.esTransicionValida(EstadoSolicitud.REGISTRADA, EstadoSolicitud.ATENDIDA),
            "No se puede saltar de REGISTRADA a ATENDIDA directamente");
        
        assertFalse(maquinaEstados.esTransicionValida(EstadoSolicitud.CLASIFICADA, EstadoSolicitud.ATENDIDA),
            "No se puede saltar de CLASIFICADA a ATENDIDA directamente");
    }
}
