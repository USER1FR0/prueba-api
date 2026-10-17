package com.proyecto.servicios.exception;

import com.proyecto.servicios.model.common.ApiResponse;
import com.proyecto.servicios.security.TokenInvalidoException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Manejo centralizado de excepciones. Todas las respuestas usan el envelope
 * ApiResponse (codigo, mensaje, datos). Las excepciones de negocio se agrupan
 * por familia (404/409/400) para no repetir handlers por cada clase concreta.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---- Validacion de @Valid en el body (incluye campos anidados) -> 400 ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleBodyValidation(MethodArgumentNotValidException ex) {
        List<String> mensajes = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Error de validacion", mensajes);
    }

    // ---- Validacion de parametros sueltos -> 400 ----
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleParamValidation(ConstraintViolationException ex) {
        List<String> mensajes = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Error de validacion", mensajes);
    }

    // ---- JSON malformado o enum invalido -> 400 ----
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleNotReadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Error de validacion",
                List.of("El cuerpo de la peticion es invalido o tiene un valor no permitido"));
    }

    // ---- Reglas de negocio (CP inexistente, password invalido) -> 400 ----
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleReglaNegocio(ReglaNegocioException ex) {
        return build(HttpStatus.BAD_REQUEST, "Error de validacion", List.of(ex.getMessage()));
    }

    // ---- Recurso no encontrado -> 404 ----
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleNoEncontrado(RecursoNoEncontradoException ex) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", List.of(ex.getMessage()));
    }

    // ---- Conflicto de negocio (duplicados, estado invalido) -> 409 ----
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleConflicto(ConflictoException ex) {
        return build(HttpStatus.CONFLICT, "Conflicto", List.of(ex.getMessage()));
    }

    // ---- Credenciales de login invalidas -> 401 ----
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleCredenciales(CredencialesInvalidasException ex) {
        return build(HttpStatus.UNAUTHORIZED, "No autorizado", List.of(ex.getMessage()));
    }

    // ---- Violaciones de integridad en BD -> 409 ----
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT, "Conflicto de integridad",
                List.of("La operacion viola una restriccion de la base de datos"));
    }

    // ---- Token JWT ausente/invalido/expirado -> 401 ----
    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleTokenInvalido(TokenInvalidoException ex) {
        return build(HttpStatus.UNAUTHORIZED, "No autorizado", List.of(ex.getMessage()));
    }

    // ---- Cualquier otro error no controlado -> 500 ----
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<List<String>>> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                List.of("Ocurrio un error inesperado"));
    }

    private ResponseEntity<ApiResponse<List<String>>> build(HttpStatus status, String mensaje, List<String> detalles) {
        return ResponseEntity.status(status).body(ApiResponse.error(status.value(), mensaje, detalles));
    }
}
