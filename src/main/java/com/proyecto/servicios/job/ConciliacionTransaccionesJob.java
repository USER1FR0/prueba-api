package com.proyecto.servicios.job;

import com.proyecto.servicios.catalogo.EstadoTransaccion;
import com.proyecto.servicios.client.gestopago.GestoPagoTxGateway;
import com.proyecto.servicios.client.gestopago.GestoPagoTxGateway.ResultadoProveedor;
import com.proyecto.servicios.entity.transaccion.Transaccion;
import com.proyecto.servicios.repositories.transaccion.TransaccionRepository;
import com.proyecto.servicios.service.transaccion.impl.RegistroSaldoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Resuelve con confirmTx las transacciones que quedaron EN_CONFIRMACION (sin respuesta de GestoPago).
 * Reglas de PuntoRed: mismos parametros que sendTx, no antes de 62 s y no despues de 24 h.
 * - codigo 06: aplicada -> el cargo queda firme.
 * - codigo 70: no aplicada -> se devuelve el saldo.
 * - otro / sin respuesta: se reintenta en la siguiente corrida.
 * Las de mas de 24 h se dejan para revision manual (el saldo sigue apartado).
 */
@Component
@Slf4j
public class ConciliacionTransaccionesJob {

    private static final long SEGUNDOS_ESPERA_MINIMA = 62;

    private final TransaccionRepository transaccionRepository;
    private final RegistroSaldoService registroSaldo;
    private final GestoPagoTxGateway gateway;
    private final ZoneId zonaHoraria;

    public ConciliacionTransaccionesJob(TransaccionRepository transaccionRepository,
                                        RegistroSaldoService registroSaldo,
                                        GestoPagoTxGateway gateway,
                                        @Value("${gestopago.tx.zona-horaria}") String zonaHoraria) {
        this.transaccionRepository = transaccionRepository;
        this.registroSaldo = registroSaldo;
        this.gateway = gateway;
        this.zonaHoraria = ZoneId.of(zonaHoraria);
    }

    @Scheduled(fixedDelayString = "${gestopago.tx.confirmacion-ms:60000}",
            initialDelayString = "${gestopago.tx.confirmacion-ms:60000}")
    public void conciliarPendientes() {
        LocalDateTime ahora = LocalDateTime.now(zonaHoraria);
        List<Transaccion> candidatas = transaccionRepository.findByEstadoAndFechaCreacionBetweenOrderByFechaCreacion(
                EstadoTransaccion.EN_CONFIRMACION, ahora.minusHours(24), ahora.minusSeconds(SEGUNDOS_ESPERA_MINIMA));
        for (Transaccion tx : candidatas) {
            conciliar(tx);
        }
    }

    private void conciliar(Transaccion tx) {
        ResultadoProveedor resultado = gateway.confirmar(tx.getPayloadEnviado());
        if (!resultado.tieneRespuesta()) {
            log.warn("confirmTx sin respuesta tx={}: {}", tx.getReferenciaOperacion(), resultado.error());
            return;
        }
        if (resultado.xml().aprobada()) {
            registroSaldo.aplicar(tx.getId(), resultado);
        } else {
            // Hay respuesta y no fue aprobada: la operacion no se aplico -> se devuelve el saldo
            registroSaldo.devolver(tx.getId(), resultado, EstadoTransaccion.RECHAZADA);
        }
    }
}
