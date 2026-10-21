package com.proyecto.servicios.exception;

/** 404: el recurso solicitado no existe (cliente, cuenta o usuario). */
public class RecursoNoEncontradoException extends NegocioException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
