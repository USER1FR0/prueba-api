package com.proyecto.servicios.exception;

public class CorreoDuplicadoException extends ConflictoException {
    public CorreoDuplicadoException(String message) {
        super(message);
    }
}
