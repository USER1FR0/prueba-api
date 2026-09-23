package com.proyecto.servicios.config;

import com.proyecto.servicios.exception.GestoPagoProductosAuthException;
import com.proyecto.servicios.exception.GestoPagoProductosException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class GestoPagoProductosErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        if (status == 401 || status == 403) {
            return new GestoPagoProductosAuthException(
                    "Error de autenticacion con GestoPago (HTTP " + status + ")");
        }

        return new GestoPagoProductosException(
                "Respuesta no exitosa de GestoPago (HTTP " + status + ")");
    }
}