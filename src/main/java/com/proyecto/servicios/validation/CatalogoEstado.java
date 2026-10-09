package com.proyecto.servicios.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida que el estado pertenezca al catalogo de entidades federativas de Mexico.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CatalogoEstadoValidator.class)
public @interface CatalogoEstado {

    String message() default "El estado no pertenece al catalogo de entidades federativas";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
