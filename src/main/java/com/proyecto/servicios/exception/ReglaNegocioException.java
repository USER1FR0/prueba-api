package com.proyecto.servicios.exception;

/** 400: la entrada rompe una regla de negocio. */
public class ReglaNegocioException extends NegocioException {
    public ReglaNegocioException(String message) {
        super(message);
    }
}
