# Documento Técnico — Onboarding de Clientes Personas Físicas

## 1. Objetivo
API REST que registra clientes persona física, valida su información y, de forma
automática, les crea una cuenta bancaria y un usuario de acceso, con autenticación JWT.

## 2. Tecnologías
- Java 21 · Spring Boot 3.3
- Spring Data JPA / Hibernate · PostgreSQL
- Flyway (migraciones V1–V5)
- Bean Validation + validadores personalizados
- Spring Security (BCrypt) · JWT (HS256)
- OpenAPI / Swagger

## 3. Arquitectura
Capas: `controller` → `service` → `repository` → `entity`.
- **controller:** expone la API REST (prefijo `/api/v1`).
- **service:** lógica de negocio y transacciones.
- **repository:** acceso a datos con JPA.
- **entity:** mapeo objeto-relacional.
- **model:** DTOs de request/response (nunca se exponen entidades directamente).
- **exception:** excepciones personalizadas + `GlobalExceptionHandler`.
- **validation:** patrones y validadores (CURP, RFC, mayoría de edad, país ISO, domicilio).

## 4. Modelo de datos
- **Cliente (1:1) Domicilio**
- **Cliente (1:1) Usuario**
- **Cliente (1:N) Cuenta**

Detalle de tablas, llaves, restricciones e índices en `02-script-bd.sql`; diagrama en `01-diagrama-ER.md`.

## 5. Reglas de negocio implementadas
- Cliente mayor de edad (18+).
- CURP, RFC y correo únicos.
- Teléfono de 10 dígitos; ingreso mensual > 0; saldo inicial ≥ 0.
- Número de cuenta único (10 dígitos, autogenerado).
- CURP, RFC y número de cuenta inmutables en la actualización.
- Solo clientes activos pueden tener cuentas activas.
- Password: mínimo 8, con mayúscula, minúscula, número y carácter especial; almacenado con BCrypt.
- Correo único entre clientes y usuarios.

## 6. Automatismos
- **Alta de cliente:** en una sola transacción se crea el cliente, su cuenta
  (ACTIVA, saldo inicial del sistema) y su usuario de acceso (correo del cliente, password cifrado).
- **Baja lógica del cliente:** el cliente queda inactivo, sus cuentas pasan a INACTIVA y su usuario se inactiva.
- **Cambio de correo del cliente:** se sincroniza con el correo del usuario de acceso.

## 7. Validaciones
- **Formato (Bean Validation):** `@NotBlank`, `@Email`, `@Pattern` (CURP, RFC, teléfono, password), `@DecimalMin`, `@Size`.
- **Personalizadas:** mayoría de edad (`@MayorEdad`), país ISO 3166 (`@PaisISO`),
  domicilio condicional por país (`@DomicilioValido`).
- **Existencia real del código postal:** se valida contra una API externa
  (zipcodestack, con respaldo zippopotam) en la capa de servicio, con caché y tolerancia a fallos.
- **Blindaje de entradas:** recorte de espacios y normalización a mayúsculas de CURP, RFC y país.

## 8. Autenticación
- `POST /api/v1/auth/login` valida correo + password contra la tabla `usuarios`,
  verifica que el usuario esté activo y emite un JWT firmado con HS256.
- El secreto del token y las credenciales sensibles se leen de variables de entorno (`.env`), nunca del código.

## 9. Manejo de excepciones
`GlobalExceptionHandler` centraliza las respuestas de error con un formato uniforme
(`timestamp`, `status`, `error`, `mensajes`, `path`). Excepciones personalizadas:
cliente/cuenta/usuario no encontrado, CURP/RFC/correo duplicado, credenciales inválidas,
contraseña inválida, código postal inválido y operación no permitida. El detalle de códigos
HTTP se encuentra en `03-guia-endpoints.md`.

## 10. Documentación y pruebas
- **Swagger UI:** `http://localhost:8081/swagger-ui.html`.
- **Guía de endpoints y payloads:** `03-guia-endpoints.md`.
- **Payloads de ejemplo:** carpeta `payloads/`.
