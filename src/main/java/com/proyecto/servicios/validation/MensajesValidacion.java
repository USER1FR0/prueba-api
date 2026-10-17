package com.proyecto.servicios.validation;

/** Mensajes de validacion reutilizables (evita duplicarlos entre DTOs y servicios). */
public final class MensajesValidacion {

    private MensajesValidacion() {
    }

    public static final String NOMBRE = "Solo admite letras y espacios (2 a 50 caracteres)";
    public static final String TELEFONO = "Debe tener exactamente 10 digitos";
    public static final String PASSWORD =
            "El password debe tener minimo 8 caracteres, una mayuscula, una minuscula, un numero y un caracter especial";
}
