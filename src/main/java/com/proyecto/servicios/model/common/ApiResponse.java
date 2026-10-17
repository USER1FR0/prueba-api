package com.proyecto.servicios.model.common;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Envelope uniforme para TODAS las respuestas (exito y error).
 * - exito: codigo = 0, mensaje = "OK", datos = payload.
 * - error: codigo = codigo HTTP, mensaje = descripcion, datos = lista de mensajes.
 */
@Schema(description = "Respuesta estandar de la API")
public record ApiResponse<T>(
        @Schema(description = "0 en exito; codigo HTTP en error", example = "0") int codigo,
        @Schema(description = "Mensaje descriptivo", example = "OK") String mensaje,
        @Schema(description = "Datos de la respuesta") T datos) {

    public static <T> ApiResponse<T> ok(T datos) {
        return new ApiResponse<>(0, "OK", datos);
    }

    public static <T> ApiResponse<T> error(int codigo, String mensaje, T datos) {
        return new ApiResponse<>(codigo, mensaje, datos);
    }
}
