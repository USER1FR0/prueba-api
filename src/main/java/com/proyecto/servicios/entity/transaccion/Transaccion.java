package com.proyecto.servicios.entity.transaccion;

import com.proyecto.servicios.catalogo.EstadoTransaccion;
import com.proyecto.servicios.entity.onboarding.Cuenta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bitacora de un pago/recarga con GestoPago. Es el libro de la operacion:
 * guarda lo enviado y lo recibido para auditoria, idempotencia y conciliacion.
 */
@Entity
@Table(name = "transacciones")
@Getter
@Setter
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Referencia publica opaca de la operacion (lo que ve el cliente). */
    @Column(name = "referencia_operacion", nullable = false, unique = true, length = 32, updatable = false)
    private String referenciaOperacion;

    /** Clave de idempotencia provista por el cliente: una misma clave = una misma operacion. */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 80, updatable = false)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false, updatable = false)
    private Cuenta cuenta;

    @Column(name = "id_servicio", nullable = false, updatable = false)
    private Integer idServicio;

    @Column(name = "id_producto", nullable = false, updatable = false)
    private Integer idProducto;

    @Column(name = "telefono", length = 20, updatable = false)
    private String telefono;

    @Column(name = "referencia", length = 100, updatable = false)
    private String referencia;

    @Column(name = "monto", nullable = false, precision = 16, scale = 2, updatable = false)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoTransaccion estado;

    /** Payload JSON en claro (antes de cifrar) para auditoria. */
    @Column(name = "payload_enviado", nullable = false, columnDefinition = "TEXT", updatable = false)
    private String payloadEnviado;

    @Column(name = "codigo_proveedor", length = 4)
    private String codigoProveedor;

    @Column(name = "texto_proveedor", length = 500)
    private String textoProveedor;

    @Column(name = "id_tx_proveedor", length = 40)
    private String idTxProveedor;

    @Column(name = "num_autorizacion", length = 60)
    private String numAutorizacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        LocalDateTime ahora = LocalDateTime.now();
        this.fechaCreacion = ahora;
        this.fechaActualizacion = ahora;
    }

    @PreUpdate
    void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
