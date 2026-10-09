# Diagrama Entidad-Relacion

Relaciones:
- Cliente **(1:1)** Domicilio
- Cliente **(1:1)** Usuario
- Cliente **(1:N)** Cuenta

```mermaid
erDiagram
    CLIENTES ||--|| DOMICILIOS : tiene
    CLIENTES ||--|| USUARIOS   : accede_con
    CLIENTES ||--o{ CUENTAS    : posee

    CLIENTES {
        bigint id PK
        varchar nombre
        varchar segundo_nombre
        varchar apellido_paterno
        varchar apellido_materno
        date fecha_nacimiento
        char curp UK
        varchar rfc UK
        varchar sexo
        varchar nacionalidad
        varchar estado_civil
        varchar correo UK
        char telefono_movil
        char telefono_alterno
        varchar ocupacion
        varchar empresa
        numeric ingreso_mensual
        boolean activo
        timestamp fecha_creacion
        timestamp fecha_actualizacion
    }

    DOMICILIOS {
        bigint id PK
        bigint cliente_id FK "UK"
        varchar calle
        varchar numero_exterior
        varchar numero_interior
        varchar colonia
        varchar municipio
        varchar estado
        varchar codigo_postal
        varchar pais "ISO 3166"
    }

    CUENTAS {
        bigint id PK
        char numero_cuenta UK
        bigint cliente_id FK
        numeric saldo
        varchar estatus
        timestamp fecha_creacion
        timestamp fecha_actualizacion
    }

    USUARIOS {
        bigint id PK
        bigint cliente_id FK "UK"
        varchar correo UK
        varchar password_hash
        boolean activo
        timestamp fecha_creacion
        timestamp fecha_actualizacion
    }
```
