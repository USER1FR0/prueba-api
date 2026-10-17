package com.proyecto.servicios.exception;

public class ClienteYaRegistradoException extends ConflictoException {
    public ClienteYaRegistradoException(String message) {
        super(message);
    }
}
