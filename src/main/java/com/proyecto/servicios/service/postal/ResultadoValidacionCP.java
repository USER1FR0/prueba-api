package com.proyecto.servicios.service.postal;

/**
 * Resultado de validar un codigo postal contra la API postal externa.
 * NO_VERIFICABLE = la API no respondio (fail-open): no se bloquea el alta.
 */
public enum ResultadoValidacionCP {
    VALIDO,
    NO_EXISTE,
    NO_VERIFICABLE
}
