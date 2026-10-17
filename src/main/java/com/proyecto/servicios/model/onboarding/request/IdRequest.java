package com.proyecto.servicios.model.onboarding.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Identificador de un recurso")
public class IdRequest {
    @Schema(example = "1")
    @NotNull(message = "El id es obligatorio")
    @Positive(message = "El id debe ser positivo")
    private Long id;
}
