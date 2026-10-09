package com.proyecto.servicios.model.onboarding.request;

import com.proyecto.servicios.catalogo.EstadoCivil;
import com.proyecto.servicios.catalogo.Sexo;
import com.proyecto.servicios.validation.MayorEdad;
import com.proyecto.servicios.validation.PatronesValidacion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Actualizacion PARCIAL de un cliente: todos los campos son opcionales,
 * pero si vienen presentes deben cumplir el formato.
 * No incluye CURP, RFC ni numero de cuenta (no son modificables).
 */
@Getter
@Setter
public class ClienteUpdateRequest {

    @Pattern(regexp = PatronesValidacion.NOMBRE, message = "El nombre solo admite letras y espacios (2 a 50 caracteres)")
    private String nombre;

    @Pattern(regexp = PatronesValidacion.NOMBRE, message = "El segundo nombre solo admite letras y espacios (2 a 50 caracteres)")
    private String segundoNombre;

    @Pattern(regexp = PatronesValidacion.NOMBRE, message = "El apellido paterno solo admite letras y espacios (2 a 50 caracteres)")
    private String apellidoPaterno;

    @Pattern(regexp = PatronesValidacion.NOMBRE, message = "El apellido materno solo admite letras y espacios (2 a 50 caracteres)")
    private String apellidoMaterno;

    @MayorEdad
    private LocalDate fechaNacimiento;

    private Sexo sexo;

    @Pattern(regexp = PatronesValidacion.NOMBRE, message = "La nacionalidad solo admite letras y espacios")
    private String nacionalidad;

    private EstadoCivil estadoCivil;

    @Email(message = "El correo debe tener un formato valido")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
    private String correo;

    @Pattern(regexp = PatronesValidacion.TELEFONO, message = "El telefono movil debe tener exactamente 10 digitos")
    private String telefonoMovil;

    @Pattern(regexp = PatronesValidacion.TELEFONO, message = "El telefono alterno debe tener exactamente 10 digitos")
    private String telefonoAlterno;

    @Size(max = 100, message = "La ocupacion no puede exceder 100 caracteres")
    private String ocupacion;

    @Size(max = 100, message = "La empresa no puede exceder 100 caracteres")
    private String empresa;

    @DecimalMin(value = "0.0", inclusive = false, message = "El ingreso mensual debe ser mayor a cero")
    @Digits(integer = 12, fraction = 2, message = "El ingreso mensual tiene un formato invalido")
    private BigDecimal ingresoMensual;

    @Valid
    private DomicilioRequest domicilio;
}
