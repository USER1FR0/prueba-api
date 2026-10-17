package com.proyecto.servicios.model.onboarding.request;

import com.proyecto.servicios.catalogo.EstatusCuenta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Actualizacion parcial de una cuenta. El numero de cuenta identifica el recurso
 * (viaja en el body, no en la URL). El saldo cambia por operaciones, no por edicion.
 */
@Getter
@Setter
public class CuentaUpdateRequest {

    @Schema(example = "0481526379")
    @NotBlank(message = "El numero de cuenta es obligatorio")
    private String numeroCuenta;

    @NotNull(message = "El estatus es obligatorio")
    private EstatusCuenta estatus;
}
