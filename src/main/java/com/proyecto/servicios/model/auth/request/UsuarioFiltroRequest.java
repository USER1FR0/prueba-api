package com.proyecto.servicios.model.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/** Filtro de usuarios por estatus (opcional). */
@Getter
@Setter
@Schema(description = "Filtro para buscar usuarios")
public class UsuarioFiltroRequest {
    @Schema(description = "true=activos, false=inactivos; null=todos")
    private Boolean activo;
}
