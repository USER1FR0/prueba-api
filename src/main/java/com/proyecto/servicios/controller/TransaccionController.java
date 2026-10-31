package com.proyecto.servicios.controller;

import com.proyecto.servicios.catalogo.EstadoTransaccion;
import com.proyecto.servicios.model.common.ApiResponse;
import com.proyecto.servicios.model.transaccion.request.ConsultaOperacionRequest;
import com.proyecto.servicios.model.transaccion.request.TransaccionRequest;
import com.proyecto.servicios.model.transaccion.response.TransaccionResponse;
import com.proyecto.servicios.service.transaccion.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Pagos y recargas con GestoPago. El cliente se toma del token (nunca del body) y toda la
 * identidad/validacion/concurrencia/conciliacion se resuelve en el service.
 */
@RestController
@RequestMapping("/api/v1/transacciones")
@Tag(name = "Transacciones", description = "Pagos y recargas vía GestoPago (sendTx), con validaciones, idempotencia y conciliación.")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @Operation(summary = "Ejecutar pago o recarga",
            description = "Valida cuenta propia y activa, saldo, producto del catálogo, topes y duplicados; "
                    + "envía la transacción cifrada a GestoPago. 200 aplicada, 202 en confirmación, "
                    + "400/403/404/409 validaciones, 422 rechazada, 503 proveedor no disponible.")
    @PostMapping
    public ResponseEntity<ApiResponse<TransaccionResponse>> pagar(@Valid @RequestBody TransaccionRequest request) {
        TransaccionResponse res = transaccionService.pagar(correoSesion(), request);
        HttpStatus status = res.estado() == EstadoTransaccion.EN_CONFIRMACION ? HttpStatus.ACCEPTED : HttpStatus.OK;
        return ResponseEntity.status(status).body(ApiResponse.ok(res));
    }

    @Operation(summary = "Consultar estado de una operación",
            description = "Devuelve el estado de una operación propia (mecanismo de consulta de la conciliación).")
    @PostMapping("/consulta")
    public ResponseEntity<ApiResponse<TransaccionResponse>> consultar(@Valid @RequestBody ConsultaOperacionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                transaccionService.consultar(correoSesion(), request.getReferenciaOperacion())));
    }

    /** Correo del usuario autenticado (lo coloca el JwtAuthenticationFilter como principal). */
    private String correoSesion() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
