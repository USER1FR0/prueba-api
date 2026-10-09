package com.proyecto.servicios.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class MayorEdadValidator implements ConstraintValidator<MayorEdad, LocalDate> {

    private int edadMinima;

    @Override
    public void initialize(MayorEdad constraintAnnotation) {
        this.edadMinima = constraintAnnotation.edadMinima();
    }

    @Override
    public boolean isValid(LocalDate fechaNacimiento, ConstraintValidatorContext context) {
        if (fechaNacimiento == null) {
            return true; // la obligatoriedad la cubre @NotNull
        }
        LocalDate fechaLimite = LocalDate.now().minusYears(edadMinima);
        return !fechaNacimiento.isAfter(fechaLimite);
    }
}
