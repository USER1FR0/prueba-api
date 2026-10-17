package com.proyecto.servicios.exception;

/** Familia 409: la operacion choca con una regla de unicidad o estado. */
public abstract class ConflictoException extends NegocioException {
    protected ConflictoException(String message) {
        super(message);
    }
}
