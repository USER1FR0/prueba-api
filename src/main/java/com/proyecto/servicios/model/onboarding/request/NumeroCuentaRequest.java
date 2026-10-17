package com.proyecto.servicios.model.onboarding.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Numero de cuenta")
public class NumeroCuentaRequest {
    @Schema(example = "0481526379")
    @NotBlank(message = "El numero de cuenta es obligatorio")
    private String numeroCuenta;
}
