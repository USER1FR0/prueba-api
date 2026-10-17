package com.proyecto.servicios.validation;

import com.proyecto.servicios.exception.ContrasenaInvalidaException;

import java.util.regex.Pattern;

/**
 * Validador reutilizable del formato de password.
 * Centraliza la regla para que servicios distintos no la dupliquen (DRY).
 */
public final class PasswordValidator {

    private static final Pattern PATRON = Pattern.compile(PatronesValidacion.PASSWORD);

    private PasswordValidator() {
    }

    public static void validar(String rawPassword) {
        if (rawPassword == null || !PATRON.matcher(rawPassword).matches()) {
            throw new ContrasenaInvalidaException(MensajesValidacion.PASSWORD);
        }
    }
}
