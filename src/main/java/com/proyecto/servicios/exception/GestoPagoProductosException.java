package com.proyecto.servicios.exception;

public class GestoPagoProductosException extends RuntimeException {

    public GestoPagoProductosException(String message) {
        super(message);
    }

    public GestoPagoProductosException(String message, Throwable cause) {
        super(message, cause);
    }
}