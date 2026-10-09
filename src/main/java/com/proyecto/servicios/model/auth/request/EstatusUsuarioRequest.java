package com.proyecto.servicios.model.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Cambio de estatus del usuario")
public class EstatusUsuarioRequest {

    @Schema(example = "false", description = "true=activo, false=inactivo")
    @NotNull(message = "El campo activo es obligatorio")
    private Boolean activo;
}
