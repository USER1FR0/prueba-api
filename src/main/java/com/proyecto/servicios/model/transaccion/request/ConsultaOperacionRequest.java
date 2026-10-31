package com.proyecto.servicios.model.transaccion.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Consulta de estado de una operacion")
public class ConsultaOperacionRequest {
    @Schema(example = "A1B2C3D4E5F6")
    @NotBlank(message = "La referencia de operacion es obligatoria")
    private String referenciaOperacion;
}
