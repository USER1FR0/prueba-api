package com.proyecto.servicios.exception;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Manejador global de excepciones.
 * Las validaciones (incluso anidadas en el domicilio) se aplanan a una
 * lista de mensajes (solo strings), sin exponer la estructura del objeto.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---- Validaciones de @Valid en el body (incluye campos anidados) ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> mensajes = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Error de validacion", mensajes, req);
    }

    // ---- Validaciones de @RequestParam / @PathVariable ----
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleParamValidation(
            ConstraintViolationException ex, HttpServletRequest req) {
        List<String> mensajes = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Error de validacion", mensajes, req);
    }

    // ---- JSON malformado o enum invalido (ej. sexo, estado civil, estatus) ----
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Error de validacion",
                List.of("El cuerpo de la peticion es invalido o tiene un valor no permitido"), req);
    }

    // ---- Tipo de parametro incorrecto (ej. id no numerico) ----
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Error de validacion",
                List.of("El parametro '" + ex.getName() + "' tiene un valor invalido"), req);
    }

    // ---- Password que no cumple la politica -> 400 ----
    @ExceptionHandler(ContrasenaInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleContrasena(
            ContrasenaInvalidaException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Error de validacion", List.of(ex.getMessage()), req);
    }

    // ---- Codigo postal que no existe segun la API postal -> 400 ----
    @ExceptionHandler(CodigoPostalInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleCodigoPostal(
            CodigoPostalInvalidoException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Error de validacion", List.of(ex.getMessage()), req);
    }

    // ---- Recursos no encontrados -> 404 ----
    @ExceptionHandler({ClienteNoEncontradoException.class, CuentaNoEncontradaException.class, UsuarioNoEncontradoException.class})
    public ResponseEntity<ErrorResponse> handleNoEncontrado(
            RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", List.of(ex.getMessage()), req);
    }

    // ---- Credenciales de login invalidas -> 401 ----
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> handleCredenciales(
            CredencialesInvalidasException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "No autorizado", List.of(ex.getMessage()), req);
    }

    // ---- Conflictos de negocio -> 409 ----
    @ExceptionHandler({
            CurpDuplicadaException.class,
            RfcDuplicadoException.class,
            CorreoDuplicadoException.class,
            ClienteYaRegistradoException.class,
            OperacionNoPermitidaException.class
    })
    public ResponseEntity<ErrorResponse> handleConflicto(
            RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Conflicto", List.of(ex.getMessage()), req);
    }

    // ---- Violaciones de integridad en BD (unique, check) -> 409 ----
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Conflicto de integridad",
                List.of("La operacion viola una restriccion de la base de datos"), req);
    }

    // ---- Cualquier otro error no controlado -> 500 ----
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                List.of("Ocurrio un error inesperado"), req);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String error, List<String> mensajes, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(status.value(), error, mensajes, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
