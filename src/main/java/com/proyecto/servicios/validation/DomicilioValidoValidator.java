package com.proyecto.servicios.validation;

import com.proyecto.servicios.catalogo.EstadoMexico;
import com.proyecto.servicios.model.onboarding.request.DomicilioRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class DomicilioValidoValidator implements ConstraintValidator<DomicilioValido, DomicilioRequest> {

    private static final String MEXICO = "MX";
    private static final Pattern CP_MX = Pattern.compile("^[0-9]{5}$");
    private static final Pattern CP_INTERNACIONAL = Pattern.compile("^[0-9A-Za-z][0-9A-Za-z \\-]{1,8}[0-9A-Za-z]$");

    @Override
    public boolean isValid(DomicilioRequest domicilio, ConstraintValidatorContext context) {
        if (domicilio == null) {
            return true; // @NotNull en el request padre cubre la ausencia
        }

        String pais = domicilio.getPais();
        String estado = domicilio.getEstado();
        String cp = domicilio.getCodigoPostal();

        // Sin pais valido no se puede decidir el resto; @PaisISO/@NotBlank reportan ese caso.
        if (pais == null || pais.isBlank()) {
            return true;
        }

        boolean esMexico = MEXICO.equalsIgnoreCase(pais.trim());
        boolean valido = true;

        context.disableDefaultConstraintViolation();

        if (esMexico) {
            if (estado == null || estado.isBlank() || !EstadoMexico.esValido(estado)) {
                context.buildConstraintViolationWithTemplate(
                                "Para Mexico el estado debe pertenecer al catalogo de estados")
                        .addPropertyNode("estado")
                        .addConstraintViolation();
                valido = false;
            }
            if (cp == null || !CP_MX.matcher(cp.trim()).matches()) {
                context.buildConstraintViolationWithTemplate(
                                "Para Mexico el codigo postal debe tener exactamente 5 digitos")
                        .addPropertyNode("codigoPostal")
                        .addConstraintViolation();
                valido = false;
            }
        } else {
            if (cp == null || !CP_INTERNACIONAL.matcher(cp.trim()).matches()) {
                context.buildConstraintViolationWithTemplate(
                                "El codigo postal internacional debe tener entre 3 y 10 caracteres alfanumericos")
                        .addPropertyNode("codigoPostal")
                        .addConstraintViolation();
                valido = false;
            }
        }

        return valido;
    }
}
