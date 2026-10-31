-- =====================================================================
-- V6: Pagos con GestoPago (sendTx) + control de concurrencia de saldo
-- =====================================================================

-- Concurrencia optimista sobre el saldo de la cuenta
ALTER TABLE cuentas ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Bitacora de transacciones
CREATE TABLE transacciones (
    id                   BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    referencia_operacion VARCHAR(32)   NOT NULL,
    idempotency_key      VARCHAR(80)   NOT NULL,
    cuenta_id            BIGINT        NOT NULL,
    id_servicio          INTEGER       NOT NULL,
    id_producto          INTEGER       NOT NULL,
    telefono             VARCHAR(20),
    referencia           VARCHAR(100),
    monto                NUMERIC(16,2) NOT NULL,
    estado               VARCHAR(20)   NOT NULL,
    payload_enviado      TEXT          NOT NULL,
    codigo_proveedor     VARCHAR(4),
    texto_proveedor      VARCHAR(500),
    id_tx_proveedor      VARCHAR(40),
    num_autorizacion     VARCHAR(60),
    fecha_creacion       TIMESTAMP     NOT NULL DEFAULT NOW(),
    fecha_actualizacion  TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_tx_referencia  UNIQUE (referencia_operacion),
    CONSTRAINT uq_tx_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_tx_cuenta      FOREIGN KEY (cuenta_id) REFERENCES cuentas (id),
    CONSTRAINT ck_tx_monto       CHECK (monto > 0),
    CONSTRAINT ck_tx_estado      CHECK (estado IN ('PENDIENTE','EN_CONFIRMACION','APLICADA','RECHAZADA','NO_ENVIADA'))
);

CREATE INDEX ix_tx_cuenta  ON transacciones (cuenta_id);
CREATE INDEX ix_tx_estado  ON transacciones (estado, fecha_creacion);
