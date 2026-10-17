package com.proyecto.servicios.exception;

/** Familia 404: el recurso solicitado no existe. */
public abstract class RecursoNoEncontradoException extends NegocioException {
    protected RecursoNoEncontradoException(String message) {
        super(message);
    }
}
