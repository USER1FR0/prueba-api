package com.proyecto.servicios.model.transaccion.response;

import com.proyecto.servicios.catalogo.EstadoTransaccion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Resultado de la operacion. Nunca expone el texto crudo del proveedor en casos de error interno. */
@Schema(description = "Resultado de un pago o recarga")
public record TransaccionResponse(
        String referenciaOperacion,
        EstadoTransaccion estado,
        String numeroCuenta,
        BigDecimal saldoCuenta,
        Integer idServicio,
        Integer idProducto,
        BigDecimal monto,
        String numAutorizacion,
        String codigoProveedor,
        LocalDateTime fecha) {
}
