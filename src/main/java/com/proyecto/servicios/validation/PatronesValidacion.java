package com.proyecto.servicios.validation;

/**
 * Expresiones regulares reutilizables para las validaciones del onboarding.
 */
public final class PatronesValidacion {

    private PatronesValidacion() {
    }

    /** Solo letras (incluye acentos y enie) y espacios, de 2 a 50 caracteres. */
    public static final String NOMBRE = "^[A-Za-zA-Za-z" + "À-ſ" + " ]{2,50}$";

    /** CURP: 4 letras, 6 digitos, 6 letras, 2 alfanumericos (18 caracteres). */
    public static final String CURP = "^[A-Z]{4}[0-9]{6}[A-Z]{6}[0-9A-Z]{2}$";

    /** RFC persona fisica/moral: 3 o 4 letras, 6 digitos, 3 alfanumericos (12-13). */
    public static final String RFC = "^[A-ZN&]{3,4}[0-9]{6}[A-Z0-9]{3}$";

    /** Telefono de exactamente 10 digitos. */
    public static final String TELEFONO = "^[0-9]{10}$";

    /** Codigo postal de exactamente 5 digitos. */
    public static final String CODIGO_POSTAL = "^[0-9]{5}$";
}
