package com.proyecto.servicios.model.transaccion.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Solicitud de pago/recarga. El cliente se toma del token, nunca del body.
 * La idempotencyKey garantiza que un reintento no genere un doble cargo.
 */
@Getter
@Setter
@Schema(description = "Datos para ejecutar un pago o recarga")
public class TransaccionRequest {

    @Schema(example = "a1b2c3d4-e5f6-7890", description = "Clave unica del cliente para esta operacion")
    @NotBlank(message = "La idempotency-key es obligatoria")
    @Size(max = 80, message = "La idempotency-key no puede exceder 80 caracteres")
    private String idempotencyKey;

    @Schema(example = "0481526379")
    @NotBlank(message = "El numero de cuenta es obligatorio")
    private String numeroCuenta;

    @NotNull(message = "El id de servicio es obligatorio")
    @Positive(message = "El id de servicio debe ser positivo")
    private Integer idServicio;

    @NotNull(message = "El id de producto es obligatorio")
    @Positive(message = "El id de producto debe ser positivo")
    private Integer idProducto;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a cero")
    @Digits(integer = 14, fraction = 2, message = "El monto tiene un formato invalido")
    private BigDecimal monto;

    @Schema(description = "Telefono/linea a recargar (recargas)")
    @Size(max = 20, message = "El telefono no puede exceder 20 caracteres")
    private String telefono;

    @Schema(description = "Referencia del servicio a pagar (pago de servicios)")
    @Size(max = 100, message = "La referencia no puede exceder 100 caracteres")
    private String referencia;
}
