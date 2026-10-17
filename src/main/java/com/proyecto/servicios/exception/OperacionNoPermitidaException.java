package com.proyecto.servicios.exception;

public class OperacionNoPermitidaException extends ConflictoException {
    public OperacionNoPermitidaException(String message) {
        super(message);
    }
}
