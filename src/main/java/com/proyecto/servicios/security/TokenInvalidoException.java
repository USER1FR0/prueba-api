package com.proyecto.servicios.security;

/** Token JWT ausente, mal formado, con firma invalida o expirado. */
public class TokenInvalidoException extends RuntimeException {
    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}
