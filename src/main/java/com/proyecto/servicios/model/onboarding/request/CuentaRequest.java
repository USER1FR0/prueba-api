package com.proyecto.servicios.model.onboarding.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Alta de una cuenta asociada a un cliente.
 * El saldo inicial es opcional; si no viene, el sistema usa el valor configurado.
 */
@Getter
@Setter
public class CuentaRequest {

    @NotNull(message = "El clienteId es obligatorio")
    private Long clienteId;

    @DecimalMin(value = "0.0", inclusive = true, message = "El saldo inicial no puede ser negativo")
    @Digits(integer = 14, fraction = 2, message = "El saldo inicial tiene un formato invalido")
    private BigDecimal saldoInicial;
}
