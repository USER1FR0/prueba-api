package com.proyecto.servicios.exception;

/** 422: la operacion fue rechazada por el proveedor (GestoPago). */
public class TransaccionRechazadaException extends NegocioException {
    public TransaccionRechazadaException(String message) {
        super(message);
    }
}
