package com.proyecto.servicios.model.onboarding.request;

import com.proyecto.servicios.catalogo.EstadoCivil;
import com.proyecto.servicios.catalogo.Sexo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.proyecto.servicios.validation.MayorEdad;
import com.proyecto.servicios.validation.PatronesValidacion;
import com.proyecto.servicios.validation.UpperCaseDeserializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ClienteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = PatronesValidacion.NOMBRE, message = "El nombre solo admite letras y espacios (2 a 50 caracteres)")
    private String nombre;

    @Pattern(regexp = PatronesValidacion.NOMBRE, message = "El segundo nombre solo admite letras y espacios (2 a 50 caracteres)")
    private String segundoNombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Pattern(regexp = PatronesValidacion.NOMBRE, message = "El apellido paterno solo admite letras y espacios (2 a 50 caracteres)")
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Pattern(regexp = PatronesValidacion.NOMBRE, message = "El apellido materno solo admite letras y espacios (2 a 50 caracteres)")
    private String apellidoMaterno;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @MayorEdad
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La CURP es obligatoria")
    @Pattern(regexp = PatronesValidacion.CURP, message = "La CURP debe tener 18 caracteres con formato valido")
    @JsonDeserialize(using = UpperCaseDeserializer.class)
    private String curp;

    @NotBlank(message = "El RFC es obligatorio")
    @Pattern(regexp = PatronesValidacion.RFC, message = "El RFC debe tener 12 o 13 caracteres con formato valido")
    @JsonDeserialize(using = UpperCaseDeserializer.class)
    private String rfc;

    @NotNull(message = "El sexo es obligatorio")
    private Sexo sexo;

    @NotBlank(message = "La nacionalidad es obligatoria")
    @Pattern(regexp = PatronesValidacion.NOMBRE, message = "La nacionalidad solo admite letras y espacios")
    private String nacionalidad;

    @NotNull(message = "El estado civil es obligatorio")
    private EstadoCivil estadoCivil;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato valido")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
    private String correo;

    @NotBlank(message = "El telefono movil es obligatorio")
    @Pattern(regexp = PatronesValidacion.TELEFONO, message = "El telefono movil debe tener exactamente 10 digitos")
    private String telefonoMovil;

    @Pattern(regexp = PatronesValidacion.TELEFONO, message = "El telefono alterno debe tener exactamente 10 digitos")
    private String telefonoAlterno;

    @NotBlank(message = "La ocupacion es obligatoria")
    @Size(max = 100, message = "La ocupacion no puede exceder 100 caracteres")
    private String ocupacion;

    @NotBlank(message = "La empresa es obligatoria")
    @Size(max = 100, message = "La empresa no puede exceder 100 caracteres")
    private String empresa;

    @NotNull(message = "El ingreso mensual es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El ingreso mensual debe ser mayor a cero")
    @Digits(integer = 12, fraction = 2, message = "El ingreso mensual tiene un formato invalido")
    private BigDecimal ingresoMensual;

    @NotNull(message = "El domicilio es obligatorio")
    @Valid
    private DomicilioRequest domicilio;

    @NotBlank(message = "El password es obligatorio")
    @Pattern(regexp = PatronesValidacion.PASSWORD,
            message = "El password debe tener minimo 8 caracteres, una mayuscula, una minuscula, un numero y un caracter especial")
    private String password;
}
