package com.proyecto.servicios.service.transaccion;

import com.proyecto.servicios.model.transaccion.request.TransaccionRequest;
import com.proyecto.servicios.model.transaccion.response.TransaccionResponse;

public interface TransaccionService {

    /** Ejecuta un pago/recarga para el cliente autenticado. */
    TransaccionResponse pagar(String correoAutenticado, TransaccionRequest request);

    /** Consulta el estado de una operacion (solo del propio cliente). */
    TransaccionResponse consultar(String correoAutenticado, String referenciaOperacion);
}
