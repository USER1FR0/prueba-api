package com.proyecto.servicios.catalogo;

/**
 * Ciclo de vida de un pago con GestoPago.
 * - PENDIENTE: saldo apartado, aun no se envia o no se sabe el resultado.
 * - EN_CONFIRMACION: se envio pero GestoPago no respondio; se resolvera con confirmTx.
 * - APLICADA: el cargo quedo firme.
 * - RECHAZADA: no se aplico; el saldo se devolvio.
 * - NO_ENVIADA: nunca salio la peticion (sin token/cifrado); el saldo se devolvio.
 */
public enum EstadoTransaccion {
    PENDIENTE, EN_CONFIRMACION, APLICADA, RECHAZADA, NO_ENVIADA
}
