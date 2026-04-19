package com.triage.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba de integración del contexto Spring Boot.
 * 
 * Verifica que el contexto completo de la aplicación pueda inicializarse correctamente
 * en entorno de test, usando la configuración específica del perfil 'test' con H2 en memoria.
 * 
 * Esta prueba no requiere una base de datos real ni configuración local del desarrollador.
 */
@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // Si llegamos aquí sin excepciones, el contexto Spring Boot se cargó correctamente
        // Esto verifica que:
        // - Todos los beans se configuraron sin conflictos
        // - JPA/Hibernate inicializó correctamente con H2
        // - Todas las dependencias se inyectaron adecuadamente
        // - La seguridad y validación están configuradas
    }

}

