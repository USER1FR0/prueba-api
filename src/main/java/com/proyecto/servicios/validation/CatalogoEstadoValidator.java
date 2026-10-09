package com.proyecto.servicios.validation;

import com.proyecto.servicios.catalogo.EstadoMexico;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CatalogoEstadoValidator implements ConstraintValidator<CatalogoEstado, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null || valor.isBlank()) {
            return true; // la obligatoriedad la cubre @NotBlank
        }
        return EstadoMexico.esValido(valor);
    }
}
