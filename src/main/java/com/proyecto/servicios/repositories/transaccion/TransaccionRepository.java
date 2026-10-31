package com.proyecto.servicios.repositories.transaccion;

import com.proyecto.servicios.catalogo.EstadoTransaccion;
import com.proyecto.servicios.entity.transaccion.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    Optional<Transaccion> findByIdempotencyKey(String idempotencyKey);

    Optional<Transaccion> findByReferenciaOperacion(String referenciaOperacion);

    boolean existsByReferenciaOperacion(String referenciaOperacion);

    /** Ventana anti-duplicado: misma cuenta+producto+monto en operaciones recientes no finalizadas en rechazo. */
    boolean existsByCuenta_IdAndIdProductoAndMontoAndEstadoInAndFechaCreacionAfter(
            Long cuentaId, Integer idProducto, BigDecimal monto,
            List<EstadoTransaccion> estados, LocalDateTime desde);

    /** Suma de montos aplicados o en vuelo de una cuenta desde una fecha (tope diario). */
    List<Transaccion> findByCuenta_IdAndEstadoInAndFechaCreacionAfter(
            Long cuentaId, List<EstadoTransaccion> estados, LocalDateTime desde);

    /** Candidatas a conciliar con confirmTx. */
    List<Transaccion> findByEstadoAndFechaCreacionBetweenOrderByFechaCreacion(
            EstadoTransaccion estado, LocalDateTime desde, LocalDateTime hasta);
}
