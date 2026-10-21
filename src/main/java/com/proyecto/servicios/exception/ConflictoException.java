package com.proyecto.servicios.exception;

/** 409: la operacion choca con una regla de unicidad o de estado. */
public class ConflictoException extends NegocioException {
    public ConflictoException(String message) {
        super(message);
    }
}
