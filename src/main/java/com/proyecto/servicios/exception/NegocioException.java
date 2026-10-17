package com.proyecto.servicios.exception;

/** Excepcion base para errores de negocio controlados. */
public abstract class NegocioException extends RuntimeException {
    protected NegocioException(String message) {
        super(message);
    }
}
