# Guía de Endpoints y Payloads

API REST del onboarding. **Todos los criterios e identificadores viajan en el body** (sin `@PathVariable` ni `@RequestParam`). Todas las respuestas usan un envelope uniforme.

- **Swagger (local):** `http://localhost:8081/swagger-ui.html`
- **Prefijo:** `/api/v1`

## Envelope de respuesta
```json
{ "codigo": 0, "mensaje": "OK", "datos": { } }
```
- Éxito → `codigo = 0`, `datos` con el resultado.
- Error → `codigo` = HTTP (400/401/404/409/500), `mensaje` descriptivo, `datos` = lista de mensajes.

## Enumeraciones
- **sexo:** `MASCULINO` · `FEMENINO` · `OTRO`
- **estadoCivil:** `SOLTERO` · `CASADO` · `DIVORCIADO` · `VIUDO` · `UNION_LIBRE`
- **estatus (cuenta):** `ACTIVA` · `INACTIVA`
- **pais:** ISO 3166 de 2 letras (`MX`, `US`…)

---

## Autenticación

### POST /api/v1/auth/login
Valida credenciales y emite JWT. 200 / 400 / 401.
```json
{ "correo": "maria.garcia@correo.mx", "password": "Secur3!Pass#2026" }
```

---

## Clientes

### POST /api/v1/clientes — Registrar
Crea cliente + cuenta + usuario automáticamente. 201 / 400 / 409.
```json
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
    "calle": "Av. Juarez", "numeroExterior": "350", "numeroInterior": "12A",
    "colonia": "Centro", "municipio": "Guanajuato", "estado": "GUANAJUATO",
    "codigoPostal": "36000", "pais": "MX"
  }
}
```

### POST /api/v1/clientes/buscar — Búsqueda única (filtros combinables)
Un solo endpoint para todas las consultas. Sin filtros devuelve todos. Campos opcionales: `id, nombre, apellidoPaterno, apellidoMaterno, curp, rfc, correo, numeroCuenta, activo, desde, hasta`.
```json
{ "nombre": "Maria", "activo": true }
```
Otros ejemplos (uno a la vez o combinados):
```json
{ "curp": "GALM880315MDFRPR07" }
{ "rfc": "GAFL880315AB1" }
{ "correo": "maria.garcia@correo.mx" }
{ "numeroCuenta": "0481526379" }
{ "id": 1 }
{ "desde": "2026-01-01", "hasta": "2026-12-31" }
```

### PATCH /api/v1/clientes — Actualización parcial
Solo campos enviados. `id` en el body. CURP/RFC/numeroCuenta inmutables. 200 / 400 / 404 / 409.
```json
{
  "id": 1,
  "telefonoMovil": "5566778899",
  "ocupacion": "Socia Directora",
  "ingresoMensual": 65000.00,
  "estadoCivil": "DIVORCIADO",
  "domicilio": {
    "calle": "Paseo de la Presa", "numeroExterior": "890", "colonia": "Centro",
    "municipio": "Leon", "estado": "GUANAJUATO", "codigoPostal": "37000", "pais": "MX"
  }
}
```

### DELETE /api/v1/clientes — Baja lógica
Inactiva cliente + sus cuentas + su usuario. 200 / 404.
```json
{ "id": 1 }
```

---

## Cuentas

### POST /api/v1/cuentas — Crear
Solo clientes activos. `saldoInicial` opcional. 201 / 400 / 404 / 409.
```json
{ "clienteId": 1, "saldoInicial": 7500.50 }
```

### POST /api/v1/cuentas/buscar — Búsqueda (combinable)
Sin filtros devuelve las activas. Combina `numeroCuenta`, `clienteId`, `estatus`.
```json
{ "clienteId": 1, "estatus": "ACTIVA" }
```

### POST /api/v1/cuentas/saldo — Consultar saldo
```json
{ "numeroCuenta": "0481526379" }
```

### PATCH /api/v1/cuentas — Cambiar estatus
```json
{ "numeroCuenta": "0481526379", "estatus": "INACTIVA" }
```

---

## Usuarios

### POST /api/v1/usuarios/buscar — Filtrar por estatus
Sin `activo` devuelve todos. Nunca expone el password.
```json
{ "activo": true }
```

### PUT /api/v1/usuarios/estatus — Activar / inactivar
```json
{ "id": 1, "activo": false }
```

---

## Catálogo de errores
| HTTP | Caso |
|---|---|
| 400 | Formato inválido, CP inexistente, password que no cumple la política |
| 401 | Credenciales incorrectas o usuario inactivo |
| 404 | Cliente / cuenta / usuario no encontrado |
| 409 | CURP / RFC / correo duplicado, o cliente inactivo |
| 500 | Error interno no controlado |
