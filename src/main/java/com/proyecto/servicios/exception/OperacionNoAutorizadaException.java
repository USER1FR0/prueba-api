package com.proyecto.servicios.exception;

/** 403: el usuario autenticado no puede operar sobre este recurso (cuenta ajena o inactiva). */
public class OperacionNoAutorizadaException extends NegocioException {
    public OperacionNoAutorizadaException(String message) {
        super(message);
    }
}
