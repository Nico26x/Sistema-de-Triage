# 📊 Guía Completa - Sistema de Triage API

## 📑 Índice

1. [Descripción General](#descripción-general)
2. [Archivos Generados](#archivos-generados)
3. [Cómo Empezar](#cómo-empezar)
4. [Estructura de Datos](#estructura-de-datos)
5. [Guía de Endpoints](#guía-de-endpoints)
6. [Casos de Uso](#casos-de-uso)
7. [Troubleshooting](#troubleshooting)

---

## 📝 Descripción General

El **Sistema de Triage de Solicitudes Académicas** es una aplicación backend basada en Spring Boot que gestiona:

- 👤 **Usuarios**: Estudiantes, Administrativos, Coordinadores
- 📋 **Solicitudes**: Homologaciones, cambios de cupo, cancelaciones, consultas
- 🔄 **Flujos**: Desde registro hasta cierre completo de solicitud
- 📊 **Historial**: Auditoría completa de todos los cambios

**Tecnología:**
- Spring Boot 4.0.3
- MariaDB (producción) / H2 (tests)
- Spring Security (placeholder tokens en Hito 2, JWT en Hito 3)
- MockMvc para tests unitarios sin BD

---

## 📂 Archivos Generados

### 1. **Sistema_Triage_API.postman_collection.json**
   - Colección completa con 16 endpoints
   - Scripts de prueba automáticos
   - Extracción de variables para chaining
   - Ejemplos realistas
   - Importable directamente en Postman

### 2. **Sistema_Triage_Environment.postman_environment.json**
   - Archivo de entorno con variables globales
   - `baseUrl`, `token`, `userId`, `solicitudId`, `responsableId`, `email`
   - Actualiza automáticamente con cada request

### 3. **POSTMAN_README.md**
   - Guía detallada de la colección Postman
   - Flujos de trabajo documentados
   - Estructura completa de DTOs y respuestas
   - Filtros y parámetros disponibles

### 4. **CURL_EXAMPLES.sh**
   - Scripts bash con ejemplos cURL
   - Alternativa para quienes prefieren CLI
   - Todos los 17 endpoints cubiertos
   - Fácil de adaptar a otros ambientes

### 5. **API_GUIDE.md** (este archivo)
   - Guía completa de uso
   - Casos de uso reales
   - Troubleshooting

---

## 🚀 Cómo Empezar

### Opción 1: Usar Postman (Recomendado)

```bash
1. Abre Postman
2. File → Import
3. Selecciona:
   - Sistema_Triage_API.postman_collection.json
   - Sistema_Triage_Environment.postman_environment.json
4. Top right → Selecciona "Sistema de Triage - Environment"
5. Asegúrate que baseUrl = http://localhost:8080
6. ¡Comienza a hacer requests!
```

### Opción 2: Usar cURL (CLI)

```bash
1. chmod +x CURL_EXAMPLES.sh
2. ./CURL_EXAMPLES.sh
# O ejecuta comandos individuales
```

### Opción 3: Manual con curl

```bash
curl -X GET http://localhost:8080/api/v1/usuarios \
  -H "Content-Type: application/json"
```

---

## 🏗️ Estructura de Datos

### DTOs Principales

#### **RegisterRequestDTO** (Crear usuario)
```json
{
  "nombre": "Juan Pérez",
  "email": "juan@universidad.edu",
  "identificacion": "1023456789",
  "password": "SecurePassword123!",
  "rol": "ESTUDIANTE"  // ESTUDIANTE, ADMINISTRATIVO, COORDINADOR
}
```

#### **UsuarioDTO** (Respuesta usuario)
```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "juan@universidad.edu",
  "identificacion": "1023456789",
  "activo": true,
  "rol": "ESTUDIANTE"
}
```

#### **SolicitudCreateDTO** (Crear solicitud)
```json
{
  "descripcion": "Solicito homologación de asignaturas",
  "canal": "CSU",
  "solicitanteId": 1,
  "impacto": "ALTO",
  "fechaLimite": "2025-12-31T17:00:00",
  "tipo": "HOMOLOGACION"
}
```

#### **SolicitudResponseDTO** (Respuesta solicitud)
```json
{
  "id": 5,
  "descripcion": "Solicito homologación de asignaturas",
  "fechaRegistro": "2025-01-15T10:30:00",
  "estado": "REGISTRADA",
  "prioridad": "BAJA",
  "justificacionPrioridad": null,
  "canalOrigen": "CSU",
  "tipoSolicitud": "HOMOLOGACION",
  "solicitante": "Juan Pérez",
  "responsable": null,
  "fechaLimite": "2025-12-31T17:00:00",
  "impacto": "ALTO"
}
```

#### **ClasificarDTO** (Clasificar solicitud)
```json
{
  "tipoSolicitud": "HOMOLOGACION",
  "impacto": "ALTO",
  "fechaLimite": "2025-12-25T17:00:00",
  "observacion": "Clasificada como prioridad alta"
}
```

#### **AsignarDTO** (Asignar responsable)
```json
{
  "responsableId": 2
}
```

#### **CambiarEstadoDTO** (Cambiar estado)
```json
{
  "nuevoEstado": "EN_ATENCION",
  "observacion": "Iniciando atención de solicitud"
}
```

#### **CerrarDTO** (Cerrar solicitud)
```json
{
  "observacion": "Solicitud completada satisfactoriamente"
}
```

#### **HistorialEntryDTO** (Entrada en historial)
```json
{
  "fechaHora": "2025-01-15T10:30:00",
  "accion": "REGISTRO",
  "observacion": "Solicitud registrada",
  "estadoAnterior": null,
  "estadoNuevo": "REGISTRADA",
  "actorId": 1
}
```

---

## 📋 Guía de Endpoints

### 1. Autenticación

#### POST `/api/v1/auth/register`
**Registra un nuevo usuario**

Request:
```json
{
  "nombre": "María García",
  "email": "maria.garcia@universidad.edu",
  "identificacion": "9876543210",
  "password": "AnotherPassword456!",
  "rol": "ADMINISTRATIVO"
}
```

Response: `201 Created`
```json
{
  "id": 2,
  "nombre": "María García",
  "email": "maria.garcia@universidad.edu",
  "identificacion": "9876543210",
  "activo": true,
  "rol": "ADMINISTRATIVO"
}
```

---

#### POST `/api/v1/auth/login`
**Inicia sesión y obtiene token**

Request:
```json
{
  "email": "maria.garcia@universidad.edu",
  "password": "AnotherPassword456!"
}
```

Response: `200 OK`
```json
{
  "token": "placeholder-token-2-1705320600000",
  "tipo": "Bearer"
}
```

⚠️ **Nota**: Token es placeholder en Hito 2. Será JWT en Hito 3.

---

### 2. Gestión de Usuarios

#### GET `/api/v1/usuarios`
**Listar todos los usuarios**

Response: `200 OK`
```json
[
  {
    "id": 1,
    "nombre": "Juan Pérez",
    "email": "juan@universidad.edu",
    "identificacion": "1023456789",
    "activo": true,
    "rol": "ESTUDIANTE"
  },
  {
    "id": 2,
    "nombre": "María García",
    "email": "maria.garcia@universidad.edu",
    "identificacion": "9876543210",
    "activo": true,
    "rol": "ADMINISTRATIVO"
  }
]
```

---

#### GET `/api/v1/usuarios/{id}`
**Obtener detalles de un usuario**

Response: `200 OK`
```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "juan@universidad.edu",
  "identificacion": "1023456789",
  "activo": true,
  "rol": "ESTUDIANTE"
}
```

---

#### GET `/api/v1/usuarios/responsables`
**Listar usuarios activos que pueden ser responsables**

Response: `200 OK`
```json
[
  {
    "id": 2,
    "nombre": "María García",
    "email": "maria.garcia@universidad.edu",
    "identificacion": "9876543210",
    "activo": true,
    "rol": "ADMINISTRATIVO"
  },
  {
    "id": 3,
    "nombre": "Carlos López",
    "email": "carlos.lopez@universidad.edu",
    "identificacion": "1111111111",
    "activo": true,
    "rol": "COORDINADOR"
  }
]
```

---

#### PATCH `/api/v1/usuarios/{id}/activar`
**Activar un usuario**

Response: `204 No Content` (sin body)

---

#### PATCH `/api/v1/usuarios/{id}/desactivar`
**Desactivar un usuario**

Response: `204 No Content` (sin body)

---

### 3. Gestión de Solicitudes

#### POST `/api/v1/solicitudes`
**Crear una nueva solicitud**

Request:
```json
{
  "descripcion": "Solicito cancelación de la asignatura de Cálculo II",
  "canal": "CORREO",
  "solicitanteId": 1,
  "impacto": "MEDIO",
  "fechaLimite": "2025-06-30T23:59:59",
  "tipo": "CANCELACION_ASIGNATURA"
}
```

Response: `201 Created`
```json
{
  "id": 10,
  "descripcion": "Solicito cancelación de la asignatura de Cálculo II",
  "fechaRegistro": "2025-01-15T14:22:30",
  "estado": "REGISTRADA",
  "prioridad": null,
  "justificacionPrioridad": null,
  "canalOrigen": "CORREO",
  "tipoSolicitud": "CANCELACION_ASIGNATURA",
  "solicitante": "Juan Pérez",
  "responsable": null,
  "fechaLimite": "2025-06-30T23:59:59",
  "impacto": "MEDIO"
}
```

---

#### GET `/api/v1/solicitudes`
**Listar solicitudes (con filtros opcionales)**

Query parameters:
```
?estado=REGISTRADA
&prioridad=ALTA
&tipoSolicitud=HOMOLOGACION
&canalOrigen=CSU
&responsableId=2
&desde=2025-01-01T00:00:00
&hasta=2025-12-31T23:59:59
```

Response: `200 OK`
```json
[
  {
    "id": 10,
    "descripcion": "Solicito cancelación de Cálculo II",
    "fechaRegistro": "2025-01-15T14:22:30",
    "estado": "REGISTRADA",
    "prioridad": null,
    "justificacionPrioridad": null,
    "canalOrigen": "CORREO",
    "tipoSolicitud": "CANCELACION_ASIGNATURA",
    "solicitante": "Juan Pérez",
    "responsable": null,
    "fechaLimite": "2025-06-30T23:59:59",
    "impacto": "MEDIO"
  }
]
```

---

#### GET `/api/v1/solicitudes/{id}`
**Obtener detalles de una solicitud**

Response: `200 OK`
```json
{
  "id": 10,
  "descripcion": "Solicito cancelación de Cálculo II",
  "fechaRegistro": "2025-01-15T14:22:30",
  "estado": "REGISTRADA",
  "prioridad": null,
  "justificacionPrioridad": null,
  "canalOrigen": "CORREO",
  "tipoSolicitud": "CANCELACION_ASIGNATURA",
  "solicitante": "Juan Pérez",
  "responsable": null,
  "fechaLimite": "2025-06-30T23:59:59",
  "impacto": "MEDIO"
}
```

---

#### PUT `/api/v1/solicitudes/{id}/clasificar`
**Clasificar una solicitud** (REGISTRADA → CLASIFICADA)

Request:
```json
{
  "tipoSolicitud": "CANCELACION_ASIGNATURA",
  "impacto": "MEDIO",
  "fechaLimite": "2025-06-30T23:59:59",
  "observacion": "Asignatura en semana 4. Cancelación aprobada por coordinador."
}
```

Response: `200 OK`
```json
{
  "id": 10,
  "descripcion": "Solicito cancelación de Cálculo II",
  "fechaRegistro": "2025-01-15T14:22:30",
  "estado": "CLASIFICADA",
  "prioridad": "MEDIA",
  "justificacionPrioridad": "Asignatura en semana 4",
  "canalOrigen": "CORREO",
  "tipoSolicitud": "CANCELACION_ASIGNATURA",
  "solicitante": "Juan Pérez",
  "responsable": null,
  "fechaLimite": "2025-06-30T23:59:59",
  "impacto": "MEDIO"
}
```

---

#### PUT `/api/v1/solicitudes/{id}/asignar`
**Asignar un responsable a la solicitud**

Request:
```json
{
  "responsableId": 2
}
```

Response: `200 OK`
```json
{
  "id": 10,
  "descripcion": "Solicito cancelación de Cálculo II",
  "fechaRegistro": "2025-01-15T14:22:30",
  "estado": "CLASIFICADA",
  "prioridad": "MEDIA",
  "justificacionPrioridad": "Asignatura en semana 4",
  "canalOrigen": "CORREO",
  "tipoSolicitud": "CANCELACION_ASIGNATURA",
  "solicitante": "Juan Pérez",
  "responsable": "María García",
  "fechaLimite": "2025-06-30T23:59:59",
  "impacto": "MEDIO"
}
```

---

#### PUT `/api/v1/solicitudes/{id}/estado`
**Cambiar estado de la solicitud**

Request:
```json
{
  "nuevoEstado": "EN_ATENCION",
  "observacion": "Solicitud en revisión. Se contactará al estudiante."
}
```

Estados válidos: `REGISTRADA`, `CLASIFICADA`, `EN_ATENCION`, `ATENDIDA`, `CERRADA`

Response: `200 OK`
```json
{
  "id": 10,
  "descripcion": "Solicito cancelación de Cálculo II",
  "fechaRegistro": "2025-01-15T14:22:30",
  "estado": "EN_ATENCION",
  "prioridad": "MEDIA",
  "justificacionPrioridad": "Asignatura en semana 4",
  "canalOrigen": "CORREO",
  "tipoSolicitud": "CANCELACION_ASIGNATURA",
  "solicitante": "Juan Pérez",
  "responsable": "María García",
  "fechaLimite": "2025-06-30T23:59:59",
  "impacto": "MEDIO"
}
```

---

#### PUT `/api/v1/solicitudes/{id}/cerrar`
**Cerrar una solicitud** (ATENDIDA → CERRADA)

Request:
```json
{
  "observacion": "Cancelación procesada. Acta enviada al estudiante."
}
```

Response: `200 OK`
```json
{
  "id": 10,
  "descripcion": "Solicito cancelación de Cálculo II",
  "fechaRegistro": "2025-01-15T14:22:30",
  "estado": "CERRADA",
  "prioridad": "MEDIA",
  "justificacionPrioridad": "Asignatura en semana 4",
  "canalOrigen": "CORREO",
  "tipoSolicitud": "CANCELACION_ASIGNATURA",
  "solicitante": "Juan Pérez",
  "responsable": "María García",
  "fechaLimite": "2025-06-30T23:59:59",
  "impacto": "MEDIO"
}
```

---

#### GET `/api/v1/solicitudes/{id}/historial`
**Obtener historial completo de cambios en la solicitud**

Response: `200 OK`
```json
[
  {
    "fechaHora": "2025-01-15T14:22:30",
    "accion": "REGISTRO",
    "observacion": "Solicitud registrada por estudiante",
    "estadoAnterior": null,
    "estadoNuevo": "REGISTRADA",
    "actorId": 1
  },
  {
    "fechaHora": "2025-01-15T14:35:00",
    "accion": "CLASIFICACION",
    "observacion": "Asignatura en semana 4. Cancelación aprobada por coordinador.",
    "estadoAnterior": "REGISTRADA",
    "estadoNuevo": "CLASIFICADA",
    "actorId": 2
  },
  {
    "fechaHora": "2025-01-15T15:00:00",
    "accion": "ASIGNACION",
    "observacion": "Asignada a María García",
    "estadoAnterior": "CLASIFICADA",
    "estadoNuevo": "CLASIFICADA",
    "actorId": 2
  },
  {
    "fechaHora": "2025-01-15T16:30:00",
    "accion": "CAMBIO_ESTADO",
    "observacion": "Solicitud en revisión. Se contactará al estudiante.",
    "estadoAnterior": "CLASIFICADA",
    "estadoNuevo": "EN_ATENCION",
    "actorId": 2
  },
  {
    "fechaHora": "2025-01-16T10:00:00",
    "accion": "CAMBIO_ESTADO",
    "observacion": "Cancelación aprobada",
    "estadoAnterior": "EN_ATENCION",
    "estadoNuevo": "ATENDIDA",
    "actorId": 2
  },
  {
    "fechaHora": "2025-01-16T11:00:00",
    "accion": "CIERRE",
    "observacion": "Cancelación procesada. Acta enviada al estudiante.",
    "estadoAnterior": "ATENDIDA",
    "estadoNuevo": "CERRADA",
    "actorId": 2
  }
]
```

---

## 🎯 Casos de Uso

### Caso 1: Nuevo Estudiante Solicita Homologación

**Objetivo:** Estudiante nuevo solicita homologar 3 asignaturas de su programa anterior.

**Pasos:**

1. **POST /auth/register** - Registrar estudiante
   ```json
   {
     "nombre": "Pedro Martínez",
     "email": "pedro.martinez@universidad.edu",
     "identificacion": "1234567890",
     "password": "MyPassword123!",
     "rol": "ESTUDIANTE"
   }
   ```
   → Obtiene `userId: 5`

2. **POST /auth/login** - Inicia sesión
   ```json
   {
     "email": "pedro.martinez@universidad.edu",
     "password": "MyPassword123!"
   }
   ```
   → Obtiene `token: placeholder-token-5-...`

3. **POST /solicitudes** - Crear solicitud de homologación
   ```json
   {
     "descripcion": "Homologación de Cálculo I, Física I y Programación I de universidad anterior",
     "canal": "CSU",
     "solicitanteId": 5,
     "impacto": "ALTO",
     "fechaLimite": "2025-03-31T17:00:00",
     "tipo": "HOMOLOGACION"
   }
   ```
   → Obtiene `solicitudId: 15`, `estado: REGISTRADA`

4. **GET /usuarios/responsables** - Obtener responsables
   → Selecciona `responsableId: 2` (coordinador académico)

5. **PUT /solicitudes/15/clasificar** - Coordinador clasifica
   ```json
   {
     "tipoSolicitud": "HOMOLOGACION",
     "impacto": "ALTO",
     "fechaLimite": "2025-03-31T17:00:00",
     "observacion": "Homologación de 3 asignaturas del plan curricular anterior"
   }
   ```
   → `estado: REGISTRADA → CLASIFICADA`

6. **PUT /solicitudes/15/asignar** - Asignar responsable
   ```json
   {
     "responsableId": 3
   }
   ```
   → Asignada a especialista en homologaciones

7. **PUT /solicitudes/15/estado** - Cambiar a en atención
   ```json
   {
     "nuevoEstado": "EN_ATENCION",
     "observacion": "Se inicia revisión de syllabus de asignaturas anteriores"
   }
   ```

8. **PUT /solicitudes/15/estado** - Marcar como atendida
   ```json
   {
     "nuevoEstado": "ATENDIDA",
     "observacion": "Homologación aprobada por comité académico"
   }
   ```

9. **PUT /solicitudes/15/cerrar** - Cerrar solicitud
   ```json
   {
     "observacion": "Asignaturas homologadas. Acta enviada a registro."
   }
   ```
   → `estado: ATENDIDA → CERRADA`

10. **GET /solicitudes/15/historial** - Ver registro completo
    → Muestra todos los cambios y quién los realizó

---

### Caso 2: Cancelación de Asignatura

**Objetivo:** Estudiante solicita cancelación de una asignatura.

**Pasos (simplificados):**

1. **POST /solicitudes** - Crear solicitud
   ```json
   {
     "descripcion": "Solicito cancelación de Cálculo II por motivos de salud",
     "canal": "PRESENCIAL",
     "solicitanteId": 5,
     "impacto": "MEDIO",
     "fechaLimite": "2025-02-15T17:00:00",
     "tipo": "CANCELACION_ASIGNATURA"
   }
   ```

2. **PUT /solicitudes/.../clasificar** → `CLASIFICADA`
3. **PUT /solicitudes/.../asignar** → Asignar consejero académico
4. **PUT /solicitudes/.../estado** → `EN_ATENCION`
5. **PUT /solicitudes/.../estado** → `ATENDIDA`
6. **PUT /solicitudes/.../cerrar** → `CERRADA`

---

### Caso 3: Gestión de Usuarios

**Objetivo:** Administrador desactiva un usuario inactivo.

**Pasos:**

1. **GET /usuarios** - Ver lista completa
2. **GET /usuarios/{id}** - Ver detalles específicos
3. **PATCH /usuarios/{id}/desactivar** - Desactivar
4. **GET /usuarios** - Verificar cambio (sigue en lista pero `activo: false`)
5. **GET /usuarios/responsables** - Confirma que NO aparece en responsables

---

### Caso 4: Filtrado de Solicitudes

**Objetivo:** Coordinador revisa solo solicitudes de alta prioridad pendientes.

**Comando:**
```bash
GET /api/v1/solicitudes?estado=REGISTRADA&prioridad=ALTA&responsableId=2
```

**Resultado:** Solo muestra solicitudes que:
- Estado: `REGISTRADA` (no clasificadas)
- Prioridad: `ALTA` o `CRITICA`
- Responsable: Usuario 2

---

## 🔧 Troubleshooting

### P1: "Status code 404 Not Found"
**Causa:** URL incorrecta o servidor no está corriendo
```bash
# Verifica que servidor está en http://localhost:8080
curl http://localhost:8080/api/v1/usuarios
```

### P2: "{{variable}} no tiene valor"
**Causa:** El request que extrae la variable no se ejecutó antes
```
Orden correcto:
1. POST /auth/register (extrae userId)
2. GET /usuarios/{{userId}} (usa userId)
```

### P3: "Status code 400 Bad Request"
**Causa:** JSON inválido o campos faltantes
- Verifica JSON es válido: `curl http://jsonlint.com`
- Todos los campos requeridos están presentes
- Valores de enum son correctos (ej: `"ALTO"` no `"Alto"`)

### P4: "Status code 500 Internal Server Error"
**Causa:** Error en el backend
- Revisa logs del servidor: `docker logs backend` o consola Spring Boot
- Verifica que BD está accesible
- Reinicia el servidor

### P5: "Status code 409 Conflict"
**Causa:** Datos duplicados o cambio de estado inválido
- Email ya registrado → Usa otro email
- Estado transition inválida → Sigue el flujo correcto

---

## 📚 Resumen Rápido

| Recurso | GET | POST | PUT | PATCH |
|---------|-----|------|-----|-------|
| **Autenticación** | - | ✓ | - | - |
| **Usuarios** | ✓ | - | - | ✓ |
| **Solicitudes** | ✓ | ✓ | ✓ | - |
| **Historial** | ✓ | - | - | - |

---

## 📞 Soporte

Para preguntas o problemas:
1. Consulta [POSTMAN_README.md](POSTMAN_README.md)
2. Revisa logs del backend
3. Verifica estructura JSON con ejemplos
4. Prueba con valores simples antes de datos complejos

---

**Última actualización:** 2025-01-15  
**Versión API:** Hito 2 (Placeholder Tokens)  
**Autor:** Sistema de Triage - Colección Auto-generada
