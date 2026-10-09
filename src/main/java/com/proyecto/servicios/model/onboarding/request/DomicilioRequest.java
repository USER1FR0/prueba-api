package com.proyecto.servicios.model.onboarding.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.proyecto.servicios.validation.DomicilioValido;
import com.proyecto.servicios.validation.PaisISO;
import com.proyecto.servicios.validation.UpperCaseDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DomicilioValido
public class DomicilioRequest {

    @NotBlank(message = "La calle es obligatoria")
    @Size(max = 100, message = "La calle no puede exceder 100 caracteres")
    private String calle;

    @NotBlank(message = "El numero exterior es obligatorio")
    @Size(max = 10, message = "El numero exterior no puede exceder 10 caracteres")
    private String numeroExterior;

    @Size(max = 10, message = "El numero interior no puede exceder 10 caracteres")
    private String numeroInterior;

    @NotBlank(message = "La colonia es obligatoria")
    @Size(max = 100, message = "La colonia no puede exceder 100 caracteres")
    private String colonia;

    @NotBlank(message = "El municipio es obligatorio")
    @Size(max = 100, message = "El municipio no puede exceder 100 caracteres")
    private String municipio;

    // La validacion del estado depende del pais (ver @DomicilioValido):
    // obligatorio y contra catalogo solo cuando el pais es MX.
    @Size(max = 100, message = "El estado no puede exceder 100 caracteres")
    private String estado;

    @NotBlank(message = "El codigo postal es obligatorio")
    @Size(max = 10, message = "El codigo postal no puede exceder 10 caracteres")
    private String codigoPostal;

    @NotBlank(message = "El pais es obligatorio")
    @PaisISO
    @JsonDeserialize(using = UpperCaseDeserializer.class)
    private String pais;
}
