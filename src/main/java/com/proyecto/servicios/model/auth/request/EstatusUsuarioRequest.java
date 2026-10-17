package com.proyecto.servicios.model.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Cambio de estatus de un usuario")
public class EstatusUsuarioRequest {

    @Schema(example = "1")
    @NotNull(message = "El id es obligatorio")
    @Positive(message = "El id debe ser positivo")
    private Long id;

    @Schema(example = "false", description = "true=activo, false=inactivo")
    @NotNull(message = "El campo activo es obligatorio")
    private Boolean activo;
}
