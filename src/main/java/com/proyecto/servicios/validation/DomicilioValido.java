package com.proyecto.servicios.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validacion a nivel de clase para el domicilio: las reglas de estado y codigo
 * postal dependen del pais. Si el pais es MX el estado debe pertenecer al
 * catalogo de estados y el CP debe ser de 5 digitos; para cualquier otro pais
 * el CP se acepta como alfanumerico de 3 a 10 caracteres (el formato real se
 * verifica despues contra la API postal en la capa de servicio).
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DomicilioValidoValidator.class)
public @interface DomicilioValido {
    String message() default "Domicilio invalido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
