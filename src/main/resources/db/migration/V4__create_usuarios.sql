-- =====================================================================
-- V4: Tabla de usuarios para autenticacion (endpoint aislado)
--  - correo unico (login)
--  - password almacenado con hash BCrypt (nunca en claro)
--  - el usuario de prueba se siembra al arranque con el PasswordEncoder
-- =====================================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id             BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    correo         VARCHAR(100)  NOT NULL,
    password_hash  VARCHAR(100)  NOT NULL,
    activo         BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_usuarios_correo UNIQUE (correo)
);
