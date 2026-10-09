package com.proyecto.servicios.model.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Credenciales de acceso")
public class LoginRequest {

    @Schema(example = "admin@onboarding.com")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato valido")
    private String correo;

    @Schema(example = "Admin123!")
    @NotBlank(message = "El password es obligatorio")
    @Size(max = 100, message = "El password no puede exceder 100 caracteres")
    private String password;
}
