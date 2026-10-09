package com.proyecto.servicios.model.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Datos de un usuario de acceso. Nunca expone el password.
 */
@Schema(description = "Usuario de acceso del cliente")
public record UsuarioResponse(
        Long id,
        Long clienteId,
        String correo,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {}
