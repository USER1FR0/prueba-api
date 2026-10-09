-- =========================================================
-- Onboarding de clientes persona fisica
-- Tablas: clientes, domicilios, cuentas
-- =========================================================

CREATE TABLE IF NOT EXISTS clientes (
    id                   BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre               VARCHAR(50)   NOT NULL,
    segundo_nombre       VARCHAR(50),
    apellido_paterno     VARCHAR(50)   NOT NULL,
    apellido_materno     VARCHAR(50)   NOT NULL,
    fecha_nacimiento     DATE          NOT NULL,
    curp                 CHAR(18)      NOT NULL,
    rfc                  VARCHAR(13)   NOT NULL,
    sexo                 VARCHAR(10)   NOT NULL,
    nacionalidad         VARCHAR(50)   NOT NULL,
    estado_civil         VARCHAR(15)   NOT NULL,
    correo               VARCHAR(100)  NOT NULL,
    telefono_movil       CHAR(10)      NOT NULL,
    telefono_alterno     CHAR(10),
    ocupacion            VARCHAR(100)  NOT NULL,
    empresa              VARCHAR(100)  NOT NULL,
    ingreso_mensual      NUMERIC(14,2) NOT NULL,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       TIMESTAMP     NOT NULL DEFAULT NOW(),
    fecha_actualizacion  TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_clientes_curp   UNIQUE (curp),
    CONSTRAINT uq_clientes_rfc    UNIQUE (rfc),
    CONSTRAINT uq_clientes_correo UNIQUE (correo),
    CONSTRAINT ck_clientes_sexo         CHECK (sexo IN ('MASCULINO','FEMENINO','OTRO')),
    CONSTRAINT ck_clientes_estado_civil CHECK (estado_civil IN ('SOLTERO','CASADO','DIVORCIADO','VIUDO','UNION_LIBRE')),
    CONSTRAINT ck_clientes_telefono     CHECK (telefono_movil ~ '^[0-9]{10}$'),
    CONSTRAINT ck_clientes_ingreso      CHECK (ingreso_mensual > 0)
);

CREATE INDEX IF NOT EXISTS idx_clientes_apellidos  ON clientes (apellido_paterno, apellido_materno);
CREATE INDEX IF NOT EXISTS idx_clientes_nombre     ON clientes (nombre);
CREATE INDEX IF NOT EXISTS idx_clientes_fecha_alta ON clientes (fecha_creacion);

CREATE TABLE IF NOT EXISTS domicilios (
    id               BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id       BIGINT        NOT NULL,
    calle            VARCHAR(100)  NOT NULL,
    numero_exterior  VARCHAR(10)   NOT NULL,
    numero_interior  VARCHAR(10),
    colonia          VARCHAR(100)  NOT NULL,
    municipio        VARCHAR(100)  NOT NULL,
    estado           VARCHAR(100)  NOT NULL,
    codigo_postal    CHAR(5)       NOT NULL,
    pais             VARCHAR(100)  NOT NULL,

    CONSTRAINT fk_domicilios_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT uq_domicilios_cliente UNIQUE (cliente_id),
    CONSTRAINT ck_domicilios_cp      CHECK (codigo_postal ~ '^[0-9]{5}$')
);

CREATE TABLE IF NOT EXISTS cuentas (
    id                   BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_cuenta        CHAR(10)      NOT NULL,
    cliente_id           BIGINT        NOT NULL,
    saldo                NUMERIC(16,2) NOT NULL DEFAULT 0,
    estatus              VARCHAR(10)   NOT NULL DEFAULT 'ACTIVA',
    fecha_creacion       TIMESTAMP     NOT NULL DEFAULT NOW(),
    fecha_actualizacion  TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_cuentas_numero   UNIQUE (numero_cuenta),
    CONSTRAINT fk_cuentas_cliente  FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT ck_cuentas_estatus  CHECK (estatus IN ('ACTIVA','INACTIVA')),
    CONSTRAINT ck_cuentas_saldo    CHECK (saldo >= 0)
);

CREATE INDEX IF NOT EXISTS idx_cuentas_cliente ON cuentas (cliente_id);
CREATE INDEX IF NOT EXISTS idx_cuentas_estatus ON cuentas (estatus);
