# Documentación API — Onboarding de Clientes Personas Físicas

Documento técnico y guía de pruebas del proyecto integrador. Describe la solución, las reglas de negocio y **cada endpoint con un payload válido** que cubre el 100 % de los requerimientos funcionales.

- **Swagger UI (local):** `http://localhost:8081/swagger-ui.html`
- **Swagger UI (Render):** `https://<tu-servicio>.onrender.com/swagger-ui.html`
- **Prefijo de la API:** todos los endpoints de negocio viven bajo `/api/v1`.

---

## 1. Descripción de la solución

API REST que registra clientes persona física, valida su información, y **de forma automática** le crea una cuenta bancaria y un usuario de acceso. Incluye autenticación por JWT.

| Aspecto | Implementación |
|---|---|
| Lenguaje / Framework | Java 21 · Spring Boot 3.3 |
| Persistencia | Spring Data JPA / Hibernate · PostgreSQL |
| Migraciones | Flyway (V1–V5) |
| Arquitectura | Capas: `controller` → `service` → `repository` → `entity` |
| Validaciones | Bean Validation + validadores personalizados |
| Seguridad | Password con BCrypt · JWT (HS256) |
| Documentación | OpenAPI / Swagger |

### Relación de entidades

```
Cliente (1) ─── (1) Domicilio
Cliente (1) ─── (1) Usuario
Cliente (1) ─── (N) Cuenta
```

- **Cliente**: datos personales, contacto e información laboral.
- **Domicilio**: dirección del cliente (1:1).
- **Cuenta**: información bancaria y saldo (1:N). Número único autogenerado.
- **Usuario**: credenciales de acceso (1:1). Correo del cliente como usuario; password cifrado.

### Automatismos

- **Al registrar un cliente** (`POST /clientes`) se crean, en la misma transacción: su **cuenta** (estatus `ACTIVA`, saldo inicial del sistema) y su **usuario** de acceso.
- **Al dar de baja un cliente** (`DELETE /clientes/{id}`): el cliente queda inactivo, **sus cuentas pasan a `INACTIVA`** y **su usuario queda inactivo**.

---

## 2. Puntos clave antes de probar

### Guarda los valores que la API te devuelve
`POST /clientes` devuelve el **`id`** del cliente y el **`numeroCuenta`** (10 dígitos). Varios endpoints los necesitan; reemplaza los placeholders por esos valores reales.

### Reglas de validación

| Campo | Regla |
|---|---|
| Nombre / Apellidos | Obligatorios, solo letras y espacios, 2–50 caracteres |
| Fecha de nacimiento | No futura y **mayoría de edad (18+)** |
| CURP | 18 caracteres, formato válido (regex). **Inmutable** |
| RFC | 12 o 13 caracteres, formato válido (regex). **Inmutable** |
| Correo | Formato válido, máx. 100, **único** (clientes y usuarios) |
| Teléfono móvil / alterno | Exactamente 10 dígitos |
| Código postal | 5 dígitos para MX; además se valida que **exista realmente** (API externa) |
| País | Código **ISO 3166 de 2 letras** (ej. `MX`, `US`) |
| Ingreso mensual | Mayor a cero |
| Saldo inicial | No negativo (0.00 o más) |
| Password | Mín. 8, con mayúscula, minúscula, número y carácter especial |
| Número de cuenta | 10 dígitos, **único**. **Inmutable** |

### Enumeraciones

- **sexo:** `MASCULINO` · `FEMENINO` · `OTRO`
- **estadoCivil:** `SOLTERO` · `CASADO` · `DIVORCIADO` · `VIUDO` · `UNION_LIBRE`
- **estatus (cuenta):** `ACTIVA` · `INACTIVA`

### Datos sucios
Las cadenas se recortan automáticamente (espacios); CURP, RFC y país se normalizan a mayúsculas.

---

## 3. Flujo de prueba recomendado

1. `POST /clientes` → guarda `id` y `numeroCuenta`.
2. `POST /auth/login` → obtén el token.
3. Consultas de cliente y cuenta (GET).
4. `PATCH` de cliente y cuenta.
5. `POST /cuentas` (cuenta adicional).
6. `GET/PUT /usuarios`.
7. `DELETE /clientes/{id}` (baja lógica) y verifica cuentas/usuario inactivos.

---

## 4. Endpoints

### 1. POST /api/v1/clientes — Registro de cliente
**Qué cubre:** RF#1 (Registro) + RF#2 (Cuenta automática) + RF#6 (Usuario automático).
**Nota:** guarda el `id` y el `numeroCuenta` de la respuesta.

```json
POST /api/v1/clientes
Content-Type: application/json

{
  "nombre": "Maria",
  "segundoNombre": "Fernanda",
  "apellidoPaterno": "Garcia",
  "apellidoMaterno": "Lopez",
  "fechaNacimiento": "1988-03-15",
  "curp": "GALM880315MDFRPR07",
  "rfc": "GAFL880315AB1",
  "sexo": "FEMENINO",
  "nacionalidad": "Mexicana",
  "estadoCivil": "CASADO",
  "correo": "maria.garcia@correo.mx",
  "telefonoMovil": "5523456789",
  "telefonoAlterno": "5587654321",
  "ocupacion": "Contadora Publica",
  "empresa": "Despacho Fiscal Garcia y Asociados SC",
  "ingresoMensual": 42000.00,
  "password": "Secur3!Pass#2026",
  "domicilio": {
    "calle": "Av. Juarez",
    "numeroExterior": "350",
    "numeroInterior": "12A",
    "colonia": "Centro",
    "municipio": "Guanajuato",
    "estado": "GUANAJUATO",
    "codigoPostal": "36000",
    "pais": "MX"
  }
}
```
→ **201** con el cliente, su domicilio y su cuenta. Duplicado → 409; CP inexistente o password débil → 400.

---

### 2. POST /api/v1/auth/login — Inicio de sesión
**Qué cubre:** RF#7 — Login + emisión de JWT.

```json
POST /api/v1/auth/login
Content-Type: application/json

{
  "correo": "maria.garcia@correo.mx",
  "password": "Secur3!Pass#2026"
}
```
→ **200** `{ correo, tipo: "Bearer", token, emitidoEn, expiraEn, expiraEnSegundos }`. Credenciales incorrectas o usuario inactivo → 401.

---

### 3. GET /api/v1/clientes — Consultar todos
**Qué cubre:** RF#3 — Consultar todos los clientes.

```
GET /api/v1/clientes
```

---

### 4. GET /api/v1/clientes/{id} — Consultar por ID
**Qué cubre:** RF#3 — Consultar cliente por ID.

```
GET /api/v1/clientes/1
```

---

### 5. GET /api/v1/clientes?curp={curp} — Buscar por CURP
**Qué cubre:** RF#3 + Consultas solicitadas.

```
GET /api/v1/clientes?curp=GALM880315MDFRPR07
```

---

### 6. GET /api/v1/clientes?rfc={rfc} — Buscar por RFC
**Qué cubre:** RF#3 + Consultas solicitadas.

```
GET /api/v1/clientes?rfc=GAFL880315AB1
```

---

### 7. GET /api/v1/clientes?correo={correo} — Buscar por correo
**Qué cubre:** Consultas solicitadas — Buscar cliente por correo.

```
GET /api/v1/clientes?correo=maria.garcia@correo.mx
```

---

### 8. GET /api/v1/clientes?numeroCuenta={numeroCuenta} — Buscar por número de cuenta
**Qué cubre:** RF#3 — Consultar cliente por número de cuenta.
**Nota:** usa el `numeroCuenta` real del endpoint 1.

```
GET /api/v1/clientes?numeroCuenta=0481526379
```

---

### 9. GET /api/v1/clientes?nombre={nombre} — Buscar por nombre
**Qué cubre:** Endpoints mínimos (coincidencia parcial, sin distinguir mayúsculas).

```
GET /api/v1/clientes?nombre=Maria
```

---

### 10. GET /api/v1/clientes?apellidoPaterno={apellidoPaterno} — Buscar por apellido paterno
**Qué cubre:** Endpoints mínimos.

```
GET /api/v1/clientes?apellidoPaterno=Garcia
```

---

### 11. GET /api/v1/clientes?apellidoMaterno={apellidoMaterno} — Buscar por apellido materno
**Qué cubre:** Endpoints mínimos.

```
GET /api/v1/clientes?apellidoMaterno=Lopez
```

---

### 12. GET /api/v1/clientes?activo=true — Clientes activos
**Qué cubre:** Consultas solicitadas — Consultar clientes activos.

```
GET /api/v1/clientes?activo=true
```

---

### 13. GET /api/v1/clientes?desde={desde}&hasta={hasta} — Rango de fechas
**Qué cubre:** Consultas solicitadas — Clientes registrados en un rango de fechas.

```
GET /api/v1/clientes?desde=2026-01-01&hasta=2026-12-31
```

---

### 14. PATCH /api/v1/clientes/{id} — Actualización parcial
**Qué cubre:** RF#4. **CURP, RFC y número de cuenta son inmutables.** Si cambia el correo, se sincroniza con el usuario de acceso; si se envía domicilio, se revalida el CP.

```json
PATCH /api/v1/clientes/1
Content-Type: application/json

{
  "telefonoMovil": "5566778899",
  "ocupacion": "Socia Directora",
  "ingresoMensual": 65000.00,
  "estadoCivil": "DIVORCIADO",
  "domicilio": {
    "calle": "Paseo de la Presa",
    "numeroExterior": "890",
    "colonia": "Centro",
    "municipio": "Leon",
    "estado": "GUANAJUATO",
    "codigoPostal": "37000",
    "pais": "MX"
  }
}
```
→ **200**. Correo ya usado → 409; CP inexistente → 400; cliente inexistente → 404.

---

### 15. DELETE /api/v1/clientes/{id} — Baja lógica
**Qué cubre:** RF#5. No elimina datos: marca el cliente inactivo e **inactiva sus cuentas y su usuario**.

```
DELETE /api/v1/clientes/1
```
→ **204**. Cliente inexistente → 404.

---

### 16. POST /api/v1/cuentas — Crear cuenta adicional
**Qué cubre:** Endpoints mínimos — Crear una cuenta asociada a un cliente. Solo clientes **activos**. `saldoInicial` es opcional (default 0.00).

```json
POST /api/v1/cuentas
Content-Type: application/json

{
  "clienteId": 1,
  "saldoInicial": 7500.50
}
```
→ **201**. Cliente inexistente → 404; cliente inactivo → 409; saldo negativo → 400.

---

### 17. GET /api/v1/cuentas/{numeroCuenta} — Consultar cuenta por número
**Qué cubre:** Endpoints mínimos.
**Nota:** usa el `numeroCuenta` real.

```
GET /api/v1/cuentas/0481526379
```

---

### 18. GET /api/v1/cuentas/{numeroCuenta}/saldo — Consultar saldo
**Qué cubre:** Consultas solicitadas — Consultar saldo de una cuenta.

```
GET /api/v1/cuentas/0481526379/saldo
```
→ **200** `{ numeroCuenta, saldo }`.

---

### 19. GET /api/v1/cuentas?clienteId={clienteId} — Cuentas por cliente
**Qué cubre:** Endpoints mínimos — Cuentas asociadas a un cliente.

```
GET /api/v1/cuentas?clienteId=1
```

---

### 20. GET /api/v1/cuentas?estatus={estatus} — Cuentas por estatus
**Qué cubre:** Consultas solicitadas — Consultar cuentas activas. Sin parámetros, `GET /api/v1/cuentas` devuelve las activas.

```
GET /api/v1/cuentas?estatus=ACTIVA
```

---

### 21. PATCH /api/v1/cuentas/{numeroCuenta} — Actualizar estatus de cuenta
**Qué cubre:** Endpoints mínimos — Actualización parcial de la cuenta.

```json
PATCH /api/v1/cuentas/0481526379
Content-Type: application/json

{
  "estatus": "INACTIVA"
}
```
→ **200**. Estatus inválido → 400; cuenta inexistente → 404.

---

### 22. GET /api/v1/usuarios/filtro?activo={activo} — Filtrar usuarios por estatus
**Qué cubre:** Gestión de usuarios de acceso. Nunca expone el password. Sin `activo` devuelve todos.

```
GET /api/v1/usuarios/filtro?activo=true
```

---

### 23. PUT /api/v1/usuarios/{id}/estatus — Activar / inactivar usuario
**Qué cubre:** Gestión de usuarios de acceso — cambio manual de estatus.

```json
PUT /api/v1/usuarios/1/estatus
Content-Type: application/json

{
  "activo": false
}
```
→ **200** con el usuario actualizado. Usuario inexistente → 404; body sin `activo` → 400.

---

## 5. Catálogo de errores

Todas las respuestas de error comparten el formato:

```json
{
  "timestamp": "2026-10-09T12:00:00",
  "status": 400,
  "error": "Error de validacion",
  "mensajes": ["mensaje 1", "mensaje 2"],
  "path": "/api/v1/clientes"
}
```

| HTTP | Caso | Excepción |
|---|---|---|
| 400 | Formato inválido, CP inexistente, password que no cumple la política | Error de validación · `CodigoPostalInvalidoException` · `ContrasenaInvalidaException` |
| 401 | Credenciales incorrectas o usuario inactivo (login) | `CredencialesInvalidasException` |
| 404 | Cliente / cuenta / usuario no encontrado | `ClienteNoEncontradoException` · `CuentaNoEncontradaException` · `UsuarioNoEncontradoException` |
| 409 | CURP / RFC / correo duplicado, o cliente inactivo | `CurpDuplicadaException` · `RfcDuplicadoException` · `CorreoDuplicadoException` · `OperacionNoPermitidaException` |
| 500 | Error interno no controlado | — |

---

## 6. Entregables del proyecto

| Entregable | Ubicación |
|---|---|
| Diagrama entidad-relación | `entregables/01-diagrama-ER.md` |
| Script de creación de BD | `entregables/02-script-bd.sql` (y migraciones Flyway `V1`–`V5`) |
| Código fuente completo | Repositorio del proyecto |
| API REST funcional | Swagger UI (sección superior) |
| Evidencias de pruebas | Capturas de las peticiones de esta guía |
| Documento técnico | `entregables/04-documento-tecnico.md` |
