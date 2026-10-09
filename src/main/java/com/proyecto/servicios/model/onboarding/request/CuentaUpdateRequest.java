package com.proyecto.servicios.model.onboarding.request;

import com.proyecto.servicios.catalogo.EstatusCuenta;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Actualizacion parcial de una cuenta. Por ahora solo el estatus es modificable
 * (el saldo cambia por operaciones financieras, no por edicion directa).
 */
@Getter
@Setter
public class CuentaUpdateRequest {

    @NotNull(message = "El estatus es obligatorio")
    private EstatusCuenta estatus;
}
