package com.proyecto.servicios.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida que una fecha de nacimiento corresponda a una persona mayor de edad
 * (y por lo tanto, que no sea una fecha futura).
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MayorEdadValidator.class)
public @interface MayorEdad {

    String message() default "El cliente debe ser mayor de edad (18 anios o mas)";

    int edadMinima() default 18;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
