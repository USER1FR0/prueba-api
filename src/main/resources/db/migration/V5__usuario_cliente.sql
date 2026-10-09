-- =====================================================================
-- V5: Usuario ligado a Cliente (1:1)
--  - El usuario se crea automaticamente al registrar el cliente
--  - cliente_id FK unica (un solo usuario por cliente)
--  - se agrega fecha_actualizacion
--  - se elimina cualquier usuario sembrado sin cliente (modelo anterior)
-- =====================================================================

-- Limpia usuarios del modelo anterior que no estan ligados a un cliente
DELETE FROM usuarios WHERE correo = 'admin@onboarding.com';

ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS cliente_id BIGINT;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS fecha_actualizacion TIMESTAMP;

-- Si quedara algun usuario sin cliente, no se puede imponer NOT NULL; se limpia
DELETE FROM usuarios WHERE cliente_id IS NULL;

ALTER TABLE usuarios ALTER COLUMN cliente_id SET NOT NULL;

ALTER TABLE usuarios
    ADD CONSTRAINT uq_usuarios_cliente UNIQUE (cliente_id);
ALTER TABLE usuarios
    ADD CONSTRAINT fk_usuarios_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id);

CREATE INDEX IF NOT EXISTS ix_usuarios_activo ON usuarios (activo);
