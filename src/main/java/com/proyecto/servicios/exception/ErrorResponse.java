package com.proyecto.servicios.exception;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final List<String> mensajes;
    private final String path;

    public ErrorResponse(int status, String error, List<String> mensajes, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.mensajes = mensajes;
        this.path = path;
    }
}
