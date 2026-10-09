package com.proyecto.servicios.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

public class PaisISOValidator implements ConstraintValidator<PaisISO, String> {

    private static final Set<String> PAISES_ISO = Set.copyOf(Arrays.asList(Locale.getISOCountries()));

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null || valor.isBlank()) {
            return true; // la obligatoriedad la cubre @NotBlank
        }
        return PAISES_ISO.contains(valor.trim().toUpperCase());
    }
}
