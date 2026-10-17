package com.proyecto.servicios.exception;

/** Familia 400: la entrada rompe una regla de negocio. */
public abstract class ReglaNegocioException extends NegocioException {
    protected ReglaNegocioException(String message) {
        super(message);
    }
}
