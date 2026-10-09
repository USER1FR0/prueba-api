package com.proyecto.servicios.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PaisISOValidator.class)
public @interface PaisISO {
    String message() default "El pais debe ser un codigo ISO 3166 de 2 letras valido (ej. MX, US, ES)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
