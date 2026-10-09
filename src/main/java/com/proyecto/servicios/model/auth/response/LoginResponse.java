package com.proyecto.servicios.model.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Respuesta del login: payload del usuario, token y datos de expiracion.
 */
@Schema(description = "Token emitido y datos de expiracion")
public record LoginResponse(
        @Schema(description = "Correo autenticado (subject del token)", example = "admin@onboarding.com")
        String correo,
        @Schema(description = "Tipo de token para el header Authorization", example = "Bearer")
        String tipo,
        @Schema(description = "Token JWT firmado (HS256)")
        String token,
        @Schema(description = "Instante de emision (UTC)")
        Instant emitidoEn,
        @Schema(description = "Instante de expiracion (UTC)")
        Instant expiraEn,
        @Schema(description = "Vigencia del token en segundos", example = "3600")
        long expiraEnSegundos
) {}
