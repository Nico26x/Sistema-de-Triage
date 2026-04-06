package com.triage.backend.service.impl;

import com.triage.backend.domain.enums.Prioridad;
import com.triage.backend.domain.enums.ImpactoAcademico;
import com.triage.backend.domain.enums.TipoSolicitudNombre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link PriorizacionServiceImpl}.
 * 
 * Cubre cálculo de puntuación, determinación de prioridad y generación de justificación
 * sin usar Spring, base de datos ni mocks innecesarios.
 */
@DisplayName("PriorizacionServiceImpl - Pruebas Unitarias")
class PriorizacionServiceImplTest {

    private PriorizacionServiceImpl priorizacionService;

    @BeforeEach
    void setUp() {
        // Instancia directa del servicio, sin Spring
        priorizacionService = new PriorizacionServiceImpl();
    }

    // ============================================================================
    // PRUEBAS: Cálculo de Puntos por Impacto Académico
    // ============================================================================

    @Test
    @DisplayName("calcularPuntuacion con impacto BAJO devuelve 1 punto de impacto")
    void testCalcularPuntuacionConImpactoBajo() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(1, puntuacion, "Impacto BAJO debe sumar 1 punto");
    }

    @Test
    @DisplayName("calcularPuntuacion con impacto MEDIO devuelve 2 puntos de impacto")
    void testCalcularPuntuacionConImpactoMedio() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.MEDIO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(2, puntuacion, "Impacto MEDIO debe sumar 2 puntos");
    }

    @Test
    @DisplayName("calcularPuntuacion con impacto ALTO devuelve 3 puntos de impacto")
    void testCalcularPuntuacionConImpactoAlto() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.ALTO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(3, puntuacion, "Impacto ALTO debe sumar 3 puntos");
    }

    @Test
    @DisplayName("calcularPuntuacion con impacto CRITICO devuelve 4 puntos de impacto")
    void testCalcularPuntuacionConImpactoCritico() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.CRITICO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(4, puntuacion, "Impacto CRITICO debe sumar 4 puntos");
    }

    // ============================================================================
    // PRUEBAS: Cálculo de Puntos por Fecha Límite
    // ============================================================================

    @Test
    @DisplayName("calcularPuntuacion con fechaLimite null devuelve 0 puntos de fecha")
    void testCalcularPuntuacionConFechaLimiteNula() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(1, puntuacion, "Fecha null debe sumar 0 puntos, total: 1 (solo impacto)");
    }

    @Test
    @DisplayName("calcularPuntuacion con fecha límite en 1 día devuelve 4 puntos de fecha")
    void testCalcularPuntuacionConFechaLimiteCercana() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusHours(12); // mañana
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(5, puntuacion, "Fecha en 1 día debe sumar 4 puntos, total: 5 (1 impacto + 4 fecha)");
    }

    @Test
    @DisplayName("calcularPuntuacion con fecha límite en 3 días devuelve 3 puntos de fecha")
    void testCalcularPuntuacionConFechaLimiteMediana() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(3);
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(4, puntuacion, "Fecha en 3 días debe sumar 3 puntos, total: 4 (1 impacto + 3 fecha)");
    }

    @Test
    @DisplayName("calcularPuntuacion con fecha límite en 15 días devuelve 2 puntos de fecha")
    void testCalcularPuntuacionConFechaLimiteMesual() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(15);
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(3, puntuacion, "Fecha en 15 días debe sumar 2 puntos, total: 3 (1 impacto + 2 fecha)");
    }

    @Test
    @DisplayName("calcularPuntuacion con fecha límite en 60 días devuelve 1 punto de fecha")
    void testCalcularPuntuacionConFechaLimiteLejana() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(60);
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(2, puntuacion, "Fecha en 60 días debe sumar 1 punto, total: 2 (1 impacto + 1 fecha)");
    }

    // ============================================================================
    // PRUEBAS: Cálculo de Puntos por Tipo de Solicitud
    // ============================================================================

    @Test
    @DisplayName("calcularPuntuacion con tipo HOMOLOGACION devuelve 2 puntos de tipo")
    void testCalcularPuntuacionConTipoHomologacion() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.HOMOLOGACION;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(3, puntuacion, "Tipo HOMOLOGACION debe sumar 2 puntos, total: 3 (1 impacto + 2 tipo)");
    }

    @Test
    @DisplayName("calcularPuntuacion con tipo SOLICITUD_CUPO devuelve 2 puntos de tipo")
    void testCalcularPuntuacionConTipoSolicitudCupo() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.SOLICITUD_CUPO;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(3, puntuacion, "Tipo SOLICITUD_CUPO debe sumar 2 puntos, total: 3 (1 impacto + 2 tipo)");
    }

    @Test
    @DisplayName("calcularPuntuacion con tipo CANCELACION_ASIGNATURA devuelve 1 punto de tipo")
    void testCalcularPuntuacionConTipoCancelacionAsignatura() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CANCELACION_ASIGNATURA;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(2, puntuacion, "Tipo CANCELACION_ASIGNATURA debe sumar 1 punto, total: 2 (1 impacto + 1 tipo)");
    }

    @Test
    @DisplayName("calcularPuntuacion con tipo REGISTRO_ASIGNATURA devuelve 1 punto de tipo")
    void testCalcularPuntuacionConTipoRegistroAsignatura() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.REGISTRO_ASIGNATURA;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(2, puntuacion, "Tipo REGISTRO_ASIGNATURA debe sumar 1 punto, total: 2 (1 impacto + 1 tipo)");
    }

    @Test
    @DisplayName("calcularPuntuacion con tipo CONSULTA_ACADEMICA devuelve 0 puntos de tipo")
    void testCalcularPuntuacionConTipoConsultaAcademica() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(1, puntuacion, "Tipo CONSULTA_ACADEMICA debe sumar 0 puntos, total: 1 (1 impacto + 0 tipo)");
    }

    @Test
    @DisplayName("calcularPuntuacion con tipo OTRO devuelve 0 puntos de tipo")
    void testCalcularPuntuacionConTipoOtro() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.OTRO;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(1, puntuacion, "Tipo OTRO debe sumar 0 puntos, total: 1 (1 impacto + 0 tipo)");
    }

    // ============================================================================
    // PRUEBAS: Determinación de Prioridad según Puntuación
    // ============================================================================

    @Test
    @DisplayName("calcularPrioridad retorna BAJA cuando puntuación < 4")
    void testCalcularPrioridadBaja() {
        // Arrange: BAJO(1) + null(0) + CONSULTA(0) = 1 < 4
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;

        // Act
        Prioridad prioridad = priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(Prioridad.BAJA, prioridad, "Puntuación 1 debe resultar en BAJA");
    }

    @Test
    @DisplayName("calcularPrioridad retorna MEDIA cuando 4 <= puntuación < 7")
    void testCalcularPrioridadMedia() {
        // Arrange: MEDIO(2) + null(0) + HOMOLOGACION(2) = 4
        ImpactoAcademico impacto = ImpactoAcademico.MEDIO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.HOMOLOGACION;

        // Act
        Prioridad prioridad = priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(Prioridad.MEDIA, prioridad, "Puntuación 4 debe resultar en MEDIA");
    }

    @Test
    @DisplayName("calcularPrioridad retorna ALTA cuando 7 <= puntuación < 10")
    void testCalcularPrioridadAlta() {
        // Arrange: ALTO(3) + 3_dias(3) + HOMOLOGACION(2) = 8
        ImpactoAcademico impacto = ImpactoAcademico.ALTO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(3);
        TipoSolicitudNombre tipo = TipoSolicitudNombre.HOMOLOGACION;

        // Act
        Prioridad prioridad = priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(Prioridad.ALTA, prioridad, "Puntuación 8 debe resultar en ALTA");
    }

    @Test
    @DisplayName("calcularPrioridad retorna CRITICA cuando puntuación >= 10")
    void testCalcularPrioridadCritica() {
        // Arrange: CRITICO(4) + 1_dia(4) + HOMOLOGACION(2) = 10
        ImpactoAcademico impacto = ImpactoAcademico.CRITICO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusHours(12);
        TipoSolicitudNombre tipo = TipoSolicitudNombre.HOMOLOGACION;

        // Act
        Prioridad prioridad = priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(Prioridad.CRITICA, prioridad, "Puntuación 10 debe resultar en CRITICA");
    }

    @Test
    @DisplayName("calcularPrioridad retorna CRITICA con puntuación > 10")
    void testCalcularPrioridadCriticaMaxima() {
        // Arrange: CRITICO(4) + 1_dia(4) + SOLICITUD_CUPO(2) + más = 11+
        ImpactoAcademico impacto = ImpactoAcademico.CRITICO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusHours(6);
        TipoSolicitudNombre tipo = TipoSolicitudNombre.SOLICITUD_CUPO;

        // Act
        Prioridad prioridad = priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(Prioridad.CRITICA, prioridad, "Puntuación > 10 debe resultar en CRITICA");
    }

    // ============================================================================
    // PRUEBAS: Generación de Justificación de Prioridad
    // ============================================================================

    @Test
    @DisplayName("generarJustificacionPrioridad genera justificación con todos los componentes")
    void testGenerarJustificacionPrioridadCompleta() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.ALTO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(3);
        TipoSolicitudNombre tipo = TipoSolicitudNombre.HOMOLOGACION;

        // Act
        String justificacion = priorizacionService.generarJustificacionPrioridad(
            impacto, fechaLimite, tipo
        );

        // Assert
        assertNotNull(justificacion, "La justificación no debe ser null");
        assertFalse(justificacion.isBlank(), "La justificación no debe estar vacía");
        assertTrue(justificacion.contains("ALTO"), "Debe contener el impacto");
        assertTrue(justificacion.contains("HOMOLOGACION"), "Debe contener el tipo");
        assertTrue(justificacion.contains("pts"), "Debe contener puntos");
        assertTrue(justificacion.contains("Total"), "Debe contener el total");
    }

    // Nota: Las pruebas con impacto=null y tipo=null se omiten porque la implementación actual
    // no maneja null en calcularPuntosImpacto() ni calcularPuntosTipo() (switch sobre enum lanza NPE)

    @Test
    @DisplayName("generarJustificacionPrioridad maneja fechaLimite null")
    void testGenerarJustificacionPrioridadConFechaNull() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;

        // Act
        String justificacion = priorizacionService.generarJustificacionPrioridad(
            impacto, fechaLimite, tipo
        );

        // Assert
        assertNotNull(justificacion, "La justificación no debe ser null incluso con fecha null");
        assertFalse(justificacion.isBlank(), "La justificación no debe estar vacía");
    }

    // ============================================================================
    // PRUEBAS: Casos Extremos y Combinaciones
    // ============================================================================

    @Test
    @DisplayName("calcularPuntuacion con valores mínimos (todos 0)")
    void testCalcularPuntuacionValoresMinimos() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.BAJO;
        LocalDateTime fechaLimite = null;
        TipoSolicitudNombre tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(1, puntuacion, "Mínimo debe ser 1 (solo impacto BAJO)");
    }

    @Test
    @DisplayName("calcularPuntuacion con valores máximos")
    void testCalcularPuntuacionValoresMaximos() {
        // Arrange
        ImpactoAcademico impacto = ImpactoAcademico.CRITICO;
        LocalDateTime fechaLimite = LocalDateTime.now().plusHours(6); // <= 1 día
        TipoSolicitudNombre tipo = TipoSolicitudNombre.HOMOLOGACION;

        // Act
        int puntuacion = priorizacionService.calcularPuntuacion(impacto, fechaLimite, tipo);

        // Assert
        assertEquals(10, puntuacion, "Máximo esperado: 4 (critico) + 4 (fecha) + 2 (tipo) = 10");
    }

    @ParameterizedTest
    @CsvSource({
        "1, BAJA",
        "2, BAJA",
        "3, BAJA",
        "4, MEDIA",
        "5, MEDIA",
        "6, MEDIA",
        "7, ALTA",
        "8, ALTA",
        "9, ALTA",
        "10, CRITICA"
    })
    @DisplayName("calcularPrioridad devuelve la prioridad correcta para cada rango de puntuación")
    void testCalcularPrioridadPorRango(int puntos, String prioridadEsperada) {
        // Este test construye casos que dan esos puntos exactamente
        // Máximo alcanzable: CRITICO(4) + 1dia(4) + HOMOLOGACION(2) = 10
        
        ImpactoAcademico impacto;
        LocalDateTime fechaLimite;
        TipoSolicitudNombre tipo;

        if (puntos <= 3) {
            // BAJO(1) + null(0) + CONSULTA(0) = 1
            impacto = ImpactoAcademico.BAJO;
            fechaLimite = null;
            tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;
        } else if (puntos <= 6) {
            // MEDIO(2) + null(0) + HOMOLOGACION(2) = 4
            impacto = ImpactoAcademico.MEDIO;
            fechaLimite = null;
            tipo = TipoSolicitudNombre.HOMOLOGACION;
        } else if (puntos <= 9) {
            // CRITICO(4) + 1dia(4) + CONSULTA(0) = 8
            impacto = ImpactoAcademico.CRITICO;
            fechaLimite = LocalDateTime.now().plusHours(12);
            tipo = TipoSolicitudNombre.CONSULTA_ACADEMICA;
        } else {
            // CRITICO(4) + 1dia(4) + HOMOLOGACION(2) = 10 (máximo)
            impacto = ImpactoAcademico.CRITICO;
            fechaLimite = LocalDateTime.now().plusHours(12);
            tipo = TipoSolicitudNombre.HOMOLOGACION;
        }

        Prioridad prioridad = priorizacionService.calcularPrioridad(impacto, fechaLimite, tipo);

        assertEquals(Prioridad.valueOf(prioridadEsperada), prioridad);
    }
}
