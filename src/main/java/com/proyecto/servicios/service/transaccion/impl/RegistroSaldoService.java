package com.proyecto.servicios.service.transaccion.impl;

import com.proyecto.servicios.catalogo.EstadoTransaccion;
import com.proyecto.servicios.client.gestopago.GestoPagoTxGateway.ResultadoProveedor;
import com.proyecto.servicios.entity.onboarding.Cuenta;
import com.proyecto.servicios.entity.transaccion.Transaccion;
import com.proyecto.servicios.model.gestopago.SendTxRespuestaXml;
import com.proyecto.servicios.repositories.onboarding.CuentaRepository;
import com.proyecto.servicios.repositories.transaccion.TransaccionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Movimientos de saldo de los pagos. Cada metodo es una transaccion CORTA de base de datos; la llamada
 * a GestoPago ocurre ENTRE ellas (fuera de transaccion) porque puede tardar hasta 62 s.
 * El control de concurrencia es optimista (@Version en Cuenta): si dos operaciones chocan, una falla
 * con conflicto de version y la capa superior reintenta. Todos los metodos son idempotentes por estado:
 * nunca aplican ni devuelven dos veces.
 */
@Service
@Slf4j
public class RegistroSaldoService {

    private static final int MAX_TEXTO = 500;

    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;

    public RegistroSaldoService(CuentaRepository cuentaRepository, TransaccionRepository transaccionRepository) {
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
    }

    /** Descuenta el saldo (revalidando) y registra la transaccion PENDIENTE. Vacio si el saldo ya no alcanza. */
    @Transactional
    public Optional<Transaccion> apartarSaldo(Transaccion nueva, Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new IllegalStateException("No existe la cuenta " + cuentaId));
        if (cuenta.getSaldo().compareTo(nueva.getMonto()) < 0) {
            return Optional.empty();
        }
        cuenta.setSaldo(cuenta.getSaldo().subtract(nueva.getMonto()));
        cuentaRepository.save(cuenta);
        nueva.setCuenta(cuenta);
        nueva.setEstado(EstadoTransaccion.PENDIENTE);
        Transaccion guardada = transaccionRepository.save(nueva);
        log.info("Saldo apartado tx={} monto={}", guardada.getReferenciaOperacion(), guardada.getMonto());
        return Optional.of(guardada);
    }

    /** El proveedor aplico la operacion: el cargo queda firme (el saldo ya se habia descontado). */
    @Transactional
    public Transaccion aplicar(Long txId, ResultadoProveedor resultado) {
        Transaccion tx = cargar(txId);
        if (tx.getEstado() == EstadoTransaccion.APLICADA) {
            return tx;
        }
        guardarRespuesta(tx, resultado);
        tx.setEstado(EstadoTransaccion.APLICADA);
        log.info("tx={} APLICADA autorizacion={}", tx.getReferenciaOperacion(), tx.getNumAutorizacion());
        return transaccionRepository.save(tx);
    }

    /** El proveedor no aplico (o nunca se envio): se devuelve el saldo apartado. */
    @Transactional
    public Transaccion devolver(Long txId, ResultadoProveedor resultado, EstadoTransaccion estadoFinal) {
        Transaccion tx = cargar(txId);
        if (tx.getEstado() == EstadoTransaccion.APLICADA
                || tx.getEstado() == EstadoTransaccion.RECHAZADA
                || tx.getEstado() == EstadoTransaccion.NO_ENVIADA) {
            return tx; // nunca devolver dos veces ni revertir una aplicada
        }
        Cuenta cuenta = cuentaRepository.findById(tx.getCuenta().getId())
                .orElseThrow(() -> new IllegalStateException("No existe la cuenta de la transaccion " + txId));
        cuenta.setSaldo(cuenta.getSaldo().add(tx.getMonto()));
        cuentaRepository.save(cuenta);
        guardarRespuesta(tx, resultado);
        tx.setEstado(estadoFinal);
        log.info("tx={} {} saldo devuelto", tx.getReferenciaOperacion(), estadoFinal);
        return transaccionRepository.save(tx);
    }

    /** Sin respuesta del proveedor: el saldo sigue apartado hasta conciliar con confirmTx. */
    @Transactional
    public Transaccion marcarEnConfirmacion(Long txId, ResultadoProveedor resultado) {
        Transaccion tx = cargar(txId);
        if (tx.getEstado() != EstadoTransaccion.PENDIENTE) {
            return tx;
        }
        guardarRespuesta(tx, resultado);
        tx.setEstado(EstadoTransaccion.EN_CONFIRMACION);
        log.warn("tx={} EN_CONFIRMACION: {}", tx.getReferenciaOperacion(), resultado.error());
        return transaccionRepository.save(tx);
    }

    private Transaccion cargar(Long txId) {
        return transaccionRepository.findById(txId)
                .orElseThrow(() -> new IllegalStateException("No existe la transaccion " + txId));
    }

    private static void guardarRespuesta(Transaccion tx, ResultadoProveedor resultado) {
        SendTxRespuestaXml xml = resultado.xml();
        if (xml == null) {
            tx.setTextoProveedor(recortar(resultado.error(), MAX_TEXTO));
            return;
        }
        tx.setCodigoProveedor(recortar(xml.codigo(), 4));
        tx.setTextoProveedor(recortar(xml.texto(), MAX_TEXTO));
        tx.setIdTxProveedor(recortar(xml.idTransaccion(), 40));
        tx.setNumAutorizacion(recortar(xml.autorizacion(), 60));
    }

    private static String recortar(String v, int max) {
        if (v == null) return null;
        String t = v.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }
}
