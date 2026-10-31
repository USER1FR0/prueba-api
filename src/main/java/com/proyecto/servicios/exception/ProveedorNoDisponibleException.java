package com.proyecto.servicios.exception;

/** 503: no fue posible comunicarse con el proveedor externo. */
public class ProveedorNoDisponibleException extends NegocioException {
    public ProveedorNoDisponibleException(String message) {
        super(message);
    }
}
