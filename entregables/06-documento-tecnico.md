# Documento Tecnico - Onboarding de Clientes Persona Fisica

## 1. Objetivo
Registrar clientes persona fisica, validar su informacion, crear
automaticamente una cuenta bancaria asociada con saldo inicial y estatus
ACTIVA, y exponer consultas/actualizaciones via API REST.

> El modulo de usuario de acceso + login JWT (secciones 6 y 7 del enunciado)
> queda fuera de esta entrega por decision del alcance; la base de datos y la
> arquitectura ya lo contemplan para agregarse despues.

## 2. Arquitectura (capas)

| Capa        | Componentes                                                        |
|-------------|-------------------------------------------------------------------|
| Controller  | ClienteController, CuentaController                                |
| Service     | ClienteService / Impl, CuentaService / Impl                       |
| Repository  | ClienteRepository, CuentaRepository (Spring Data JPA)             |
| Entity      | Cliente, Domicilio, Cuenta                                        |
| DTO         | *Request (entrada + validaciones), *Response (salida)            |
| Mapper      | ClienteMapper, CuentaMapper (MapStruct)                           |
| Catalogo    | Sexo, EstadoCivil, EstatusCuenta, EstadoMexico                    |
| Validation  | @MayorEdad, @CatalogoEstado, PatronesValidacion                  |
| Exception   | Excepciones propias + GlobalExceptionHandler                     |

Flujo de registro (transaccional):
`POST /clientes` -> valida -> guarda Cliente + Domicilio -> crea Cuenta
automatica -> responde el cliente con su cuenta.

## 3. Base de datos
PostgreSQL. Tablas `clientes`, `domicilios`, `cuentas`. Migracion Flyway
`V2__create_onboarding.sql`.

Decisiones de tipos (memoria):
- `CHAR(n)` para longitud fija: curp(18), telefono(10), codigo_postal(5), numero_cuenta(10).
- `VARCHAR(n)` acotado para texto variable.
- `NUMERIC(14,2)` / `NUMERIC(16,2)` para dinero (nunca float).
- `DATE` para fecha de nacimiento, `TIMESTAMP` para auditoria.
- PK `BIGINT GENERATED ALWAYS AS IDENTITY`.

Integridad: PK en todas; FK cliente_id en domicilios (UNIQUE -> 1:1) y cuentas (1:N);
UNIQUE en curp, rfc, correo, numero_cuenta; CHECK para telefono/cp (solo digitos),
ingreso>0, saldo>=0 y enums validos. Indices en apellidos, nombre, fecha_creacion,
cliente_id y estatus.

## 4. Validaciones
Doble capa:
1. **Entrada (Bean Validation)** en los DTO: obligatoriedad, longitudes, formatos
   (regex CURP/RFC/telefono/cp), @Email, mayoria de edad (@MayorEdad), estado dentro
   del catalogo (@CatalogoEstado), ingreso > 0.
2. **Negocio (servicio)**: unicidad de CURP/RFC/correo, cliente activo para cuenta
   activa, baja logica que inactiva cuentas.
3. **Base de datos**: UNIQUE y CHECK como ultima linea de defensa.

Las validaciones (incluso las anidadas del domicilio) se centralizan en el
`GlobalExceptionHandler`, que las aplana a una **lista de mensajes (strings)**
en la respuesta, sin exponer la estructura del objeto.

## 5. Manejo de errores (codigos HTTP)
| Situacion                          | HTTP | Excepcion                        |
|------------------------------------|------|----------------------------------|
| Error de validacion                | 400  | MethodArgumentNotValid / ...     |
| Cliente/Cuenta no encontrado       | 404  | *NoEncontrado(a)Exception        |
| CURP/RFC/correo duplicado          | 409  | *Duplicad(a/o)Exception          |
| Operacion no permitida (reglas)    | 409  | OperacionNoPermitidaException    |
| Error inesperado                   | 500  | Exception                        |

Formato de error:
```json
{ "timestamp": "...", "status": 400, "error": "Error de validacion",
  "mensajes": ["La CURP debe tener 18 caracteres...", "..."], "path": "/clientes" }
```

## 6. Endpoints
Clientes: POST /clientes, GET /clientes (filtros por nombre, apellidoPaterno,
apellidoMaterno, curp, rfc, correo, numeroCuenta, activo, desde/hasta),
GET /clientes/{id}, PATCH /clientes/{id}, DELETE /clientes/{id} (baja logica).

Cuentas: POST /cuentas, GET /cuentas (clienteId, estatus; por defecto activas),
GET /cuentas/{numeroCuenta}, GET /cuentas/{numeroCuenta}/saldo,
PATCH /cuentas/{numeroCuenta}.

## 7. Reglas de negocio implementadas
- Mayoria de edad (18+).
- CURP, RFC y correo unicos.
- Telefono de 10 digitos; ingreso > 0; saldo inicial >= 0.
- Numero de cuenta unico (generado y verificado).
- Solo clientes activos pueden tener cuentas activas.
- Baja logica: el cliente se desactiva y sus cuentas pasan a INACTIVA.
- CURP, RFC y numero de cuenta no son modificables.

## 8. Como ejecutar
1. `gradlew bootRun` (Flyway aplica V1 y V2).
2. Swagger: http://localhost:8081/swagger-ui.html
3. Probar con la coleccion `04-pruebas.http` o los payloads de `payloads/`.
4. Pruebas unitarias: `gradlew test`.
5. Estres: `k6 run 05-pruebas-estres-k6.js`.

## 9. Decisiones tecnicas
- **MapStruct** para mapear DTO<->entidad sin codigo repetitivo; update parcial
  con `NullValuePropertyMappingStrategy.IGNORE`.
- **Catalogos como enums** (Sexo, EstadoCivil, EstatusCuenta, EstadoMexico) para
  evitar texto libre y validar en origen.
- **Saldo inicial y longitud de cuenta** configurables en `application.properties`.
- **Registro transaccional**: cliente + domicilio + cuenta en una sola transaccion.
