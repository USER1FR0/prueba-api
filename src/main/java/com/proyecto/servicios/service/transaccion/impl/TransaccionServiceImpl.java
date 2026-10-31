package com.proyecto.servicios.service.transaccion.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.catalogo.EstadoTransaccion;
import com.proyecto.servicios.catalogo.EstatusCuenta;
import com.proyecto.servicios.client.gestopago.GestoPagoTxGateway;
import com.proyecto.servicios.client.gestopago.GestoPagoTxGateway.ResultadoProveedor;
import com.proyecto.servicios.entity.mongo.ProductoDocument;
import com.proyecto.servicios.entity.onboarding.Cliente;
import com.proyecto.servicios.entity.onboarding.Cuenta;
import com.proyecto.servicios.entity.transaccion.Transaccion;
import com.proyecto.servicios.exception.ConflictoException;
import com.proyecto.servicios.exception.OperacionNoAutorizadaException;
import com.proyecto.servicios.exception.ProveedorNoDisponibleException;
import com.proyecto.servicios.exception.RecursoNoEncontradoException;
import com.proyecto.servicios.exception.ReglaNegocioException;
import com.proyecto.servicios.exception.TransaccionRechazadaException;
import com.proyecto.servicios.model.transaccion.request.TransaccionRequest;
import com.proyecto.servicios.model.transaccion.response.TransaccionResponse;
import com.proyecto.servicios.repositories.onboarding.ClienteRepository;
import com.proyecto.servicios.repositories.onboarding.CuentaRepository;
import com.proyecto.servicios.repositories.transaccion.TransaccionRepository;
import com.proyecto.servicios.service.CatalogoProductosService;
import com.proyecto.servicios.service.transaccion.TransaccionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TransaccionServiceImpl implements TransaccionService {

    private static final int TIPO_FRONT_RECARGA = 1;
    private static final int TIPO_FRONT_SERVICIO = 2;
    private static final String TELEFONO_SERVICIOS = "1111111111";
    private static final String TELEFONO_CANJEABLES = "#1111111111";
    private static final DateTimeFormatter HORA_LOCAL = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LONGITUD_REFERENCIA = 12;
    private static final int MAX_INTENTOS_APARTADO = 4;
    /** Estados que cuentan como "dinero comprometido" para tope diario y ventana anti-duplicado. */
    private static final List<EstadoTransaccion> EN_VUELO = List.of(
            EstadoTransaccion.PENDIENTE, EstadoTransaccion.EN_CONFIRMACION, EstadoTransaccion.APLICADA);

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;
    private final CatalogoProductosService catalogoProductosService;
    private final RegistroSaldoService registroSaldo;
    private final GestoPagoTxGateway gateway;
    private final ObjectMapper objectMapper;
    private final String unidad;
    private final ZoneId zonaHoraria;
    private final BigDecimal montoMaximo;
    private final BigDecimal topeDiario;
    private final int ventanaAntiduplicadoSeg;
    private final SecureRandom random = new SecureRandom();

    public TransaccionServiceImpl(ClienteRepository clienteRepository,
                                  CuentaRepository cuentaRepository,
                                  TransaccionRepository transaccionRepository,
                                  CatalogoProductosService catalogoProductosService,
                                  RegistroSaldoService registroSaldo,
                                  GestoPagoTxGateway gateway,
                                  ObjectMapper objectMapper,
                                  @Value("${gestopago.tx.unidad}") String unidad,
                                  @Value("${gestopago.tx.zona-horaria}") String zonaHoraria,
                                  @Value("${gestopago.tx.monto-maximo}") BigDecimal montoMaximo,
                                  @Value("${gestopago.tx.tope-diario}") BigDecimal topeDiario,
                                  @Value("${gestopago.tx.ventana-antiduplicado-seg}") int ventanaAntiduplicadoSeg) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
        this.catalogoProductosService = catalogoProductosService;
        this.registroSaldo = registroSaldo;
        this.gateway = gateway;
        this.objectMapper = objectMapper;
        this.unidad = unidad;
        this.zonaHoraria = ZoneId.of(zonaHoraria);
        this.montoMaximo = montoMaximo;
        this.topeDiario = topeDiario;
        this.ventanaAntiduplicadoSeg = ventanaAntiduplicadoSeg;
    }

    @Override
    public TransaccionResponse pagar(String correoAutenticado, TransaccionRequest request) {
        // 1. Idempotencia: misma clave = misma operacion (no se vuelve a cobrar)
        Optional<Transaccion> previa = transaccionRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (previa.isPresent()) {
            return segunEstado(previa.get());
        }

        // 2. Cliente de la sesion, cuenta, propiedad y estado
        Cliente cliente = clienteRepository.findByCorreo(correoAutenticado)
                .orElseThrow(() -> new OperacionNoAutorizadaException("Sesion no valida."));
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(request.getNumeroCuenta())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cuenta indicada."));
        if (!cuenta.getCliente().getId().equals(cliente.getId())) {
            log.warn("Cliente {} intento operar la cuenta del cliente {}", cliente.getId(), cuenta.getCliente().getId());
            throw new OperacionNoAutorizadaException("La cuenta no pertenece al usuario de la sesion.");
        }
        if (!Boolean.TRUE.equals(cliente.getActivo()) || cuenta.getEstatus() != EstatusCuenta.ACTIVA) {
            throw new OperacionNoAutorizadaException("El cliente o la cuenta no estan activos.");
        }

        // 3. Producto del catalogo + reglas de tipoFront/monto
        ProductoDocument producto = buscarProducto(request.getIdServicio(), request.getIdProducto());
        DatosPago datos = armarDatosPago(request, producto);

        // 4. Politicas de riesgo (nuestros extras)
        validarLimites(cuenta, request.getIdProducto(), request.getMonto());

        // 5. Apartar saldo (optimista, con reintento ante conflicto de version)
        Transaccion tx = apartarConReintento(request, datos, cuenta.getId());

        // 6. Enviar a GestoPago (fuera de transaccion)
        ResultadoProveedor resultado = gateway.enviar(tx.getPayloadEnviado());

        // 7. Resolver segun la respuesta
        return resolver(tx, resultado);
    }

    @Override
    @Transactional(readOnly = true)
    public TransaccionResponse consultar(String correoAutenticado, String referenciaOperacion) {
        Transaccion tx = transaccionRepository.findByReferenciaOperacion(referenciaOperacion)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la operacion indicada."));
        if (!tx.getCuenta().getCliente().getCorreo().equalsIgnoreCase(correoAutenticado)) {
            throw new OperacionNoAutorizadaException("La operacion no pertenece al usuario de la sesion.");
        }
        return toResponse(tx);
    }

    // ---------- Flujo interno ----------

    private TransaccionResponse resolver(Transaccion tx, ResultadoProveedor resultado) {
        if (!resultado.enviada()) {
            registroSaldo.devolver(tx.getId(), resultado, EstadoTransaccion.NO_ENVIADA);
            throw new ProveedorNoDisponibleException(
                    "No fue posible conectar con el proveedor; no se realizo ningun cargo. Intente mas tarde.");
        }
        if (!resultado.tieneRespuesta()) {
            // Sin XML legible: no se sabe si se aplico -> conciliar con confirmTx
            Transaccion porConfirmar = registroSaldo.marcarEnConfirmacion(tx.getId(), resultado);
            return toResponse(porConfirmar);
        }
        if (resultado.xml().aprobada()) {
            return toResponse(registroSaldo.aplicar(tx.getId(), resultado));
        }
        // Hay respuesta y NO fue aprobada (ej. NUM_AUTORIZACION -1): se rechaza y se devuelve el saldo
        registroSaldo.devolver(tx.getId(), resultado, EstadoTransaccion.RECHAZADA);
        throw new TransaccionRechazadaException("La operacion fue rechazada por el proveedor.");
    }

    /** Replay idempotente: devuelve el resultado almacenado con la misma semantica que el original. */
    private TransaccionResponse segunEstado(Transaccion tx) {
        return switch (tx.getEstado()) {
            case RECHAZADA -> throw new TransaccionRechazadaException("La operacion fue rechazada por el proveedor.");
            case NO_ENVIADA -> throw new ProveedorNoDisponibleException(
                    "No fue posible conectar con el proveedor; no se realizo ningun cargo.");
            default -> toResponse(tx); // APLICADA (200) o EN_CONFIRMACION (202)
        };
    }

    private Transaccion apartarConReintento(TransaccionRequest request, DatosPago datos, Long cuentaId) {
        for (int intento = 1; intento <= MAX_INTENTOS_APARTADO; intento++) {
            try {
                Transaccion nueva = construir(request, datos);
                return registroSaldo.apartarSaldo(nueva, cuentaId)
                        .orElseThrow(() -> new ConflictoException("Saldo insuficiente para realizar la operacion."));
            } catch (ObjectOptimisticLockingFailureException ex) {
                if (intento == MAX_INTENTOS_APARTADO) {
                    throw new ConflictoException("La cuenta esta siendo usada por otra operacion. Intente de nuevo.");
                }
                log.debug("Conflicto de version apartando saldo, reintento {}", intento);
            }
        }
        throw new ConflictoException("No fue posible apartar el saldo.");
    }

    private void validarLimites(Cuenta cuenta, Integer idProducto, BigDecimal monto) {
        if (monto.compareTo(montoMaximo) > 0) {
            throw new ReglaNegocioException("El monto excede el maximo permitido por operacion ("
                    + montoMaximo.setScale(2, RoundingMode.HALF_UP) + ").");
        }
        // Tope diario: suma de lo comprometido hoy + esta operacion
        LocalDateTime inicioDia = LocalDateTime.now(zonaHoraria).toLocalDate().atStartOfDay();
        BigDecimal acumulado = transaccionRepository
                .findByCuenta_IdAndEstadoInAndFechaCreacionAfter(cuenta.getId(), EN_VUELO, inicioDia)
                .stream().map(Transaccion::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (acumulado.add(monto).compareTo(topeDiario) > 0) {
            throw new ConflictoException("La operacion supera el tope diario de la cuenta ("
                    + topeDiario.setScale(2, RoundingMode.HALF_UP) + ").");
        }
        // Ventana anti-duplicado: misma cuenta+producto+monto en operaciones recientes no rechazadas
        LocalDateTime desde = LocalDateTime.now(zonaHoraria).minusSeconds(ventanaAntiduplicadoSeg);
        boolean duplicada = transaccionRepository
                .existsByCuenta_IdAndIdProductoAndMontoAndEstadoInAndFechaCreacionAfter(
                        cuenta.getId(), idProducto, monto, EN_VUELO, desde);
        if (duplicada) {
            throw new ConflictoException("Ya hay una operacion igual en proceso. Consulte su estado antes de reintentar.");
        }
    }

    private DatosPago armarDatosPago(TransaccionRequest request, ProductoDocument producto) {
        Integer tipoFront = producto.getTipoFront();
        boolean canjeable = Integer.valueOf(10).equals(producto.getIdCatTipoServicio())
                || Integer.valueOf(11).equals(producto.getIdCatTipoServicio());

        if (Integer.valueOf(TIPO_FRONT_RECARGA).equals(tipoFront)) {
            if (producto.getPrecio() != null
                    && BigDecimal.valueOf(producto.getPrecio()).compareTo(request.getMonto()) != 0) {
                throw new ReglaNegocioException("El monto debe ser igual al precio del producto: "
                        + BigDecimal.valueOf(producto.getPrecio()).setScale(2, RoundingMode.HALF_UP) + ".");
            }
            String telefono;
            if (canjeable) {
                telefono = TELEFONO_CANJEABLES;
            } else if (request.getTelefono() == null || request.getTelefono().isBlank()) {
                throw new ReglaNegocioException("El telefono es obligatorio para este producto.");
            } else {
                telefono = request.getTelefono();
            }
            return new DatosPago(telefono, null, false);
        }
        if (Integer.valueOf(TIPO_FRONT_SERVICIO).equals(tipoFront)) {
            if (request.getReferencia() == null || request.getReferencia().isBlank()) {
                throw new ReglaNegocioException("La referencia es obligatoria para el pago de este servicio.");
            }
            return new DatosPago(canjeable ? TELEFONO_CANJEABLES : TELEFONO_SERVICIOS, request.getReferencia(), true);
        }
        throw new ReglaNegocioException("El tipo de producto no se puede pagar con este servicio.");
    }

    private Transaccion construir(TransaccionRequest request, DatosPago datos) {
        Transaccion tx = new Transaccion();
        tx.setReferenciaOperacion(generarReferencia());
        tx.setIdempotencyKey(request.getIdempotencyKey());
        tx.setIdServicio(request.getIdServicio());
        tx.setIdProducto(request.getIdProducto());
        tx.setTelefono(datos.telefono());
        tx.setReferencia(datos.referencia());
        tx.setMonto(request.getMonto());
        tx.setPayloadEnviado(payloadJson(tx, datos));
        return tx;
    }

    /** JSON con el orden y los nombres de la documentacion de sendTx; solo los campos que aplican. */
    private String payloadJson(Transaccion tx, DatosPago datos) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("idProducto", tx.getIdProducto());
        payload.put("idServicio", tx.getIdServicio());
        payload.put("telefono", tx.getTelefono());
        payload.put("horaLocal", HORA_LOCAL.format(ZonedDateTime.now(zonaHoraria)));
        if (datos.pagoServicio()) {
            payload.put("referencia", tx.getReferencia());
            payload.put("montoPago", tx.getMonto());
        }
        payload.put("upc", tx.getReferenciaOperacion());
        payload.put("unidad", unidad);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible armar el payload de sendTx", e);
        }
    }

    private ProductoDocument buscarProducto(Integer idServicio, Integer idProducto) {
        List<ProductoDocument> catalogo;
        try {
            catalogo = catalogoProductosService.consultar();
        } catch (RuntimeException ex) {
            log.error("Catalogo de GestoPago no disponible: {}", ex.getMessage());
            throw new ProveedorNoDisponibleException("El catalogo de productos no esta disponible. Intente mas tarde.");
        }
        return catalogo.stream()
                .filter(p -> idServicio.equals(p.getIdServicio()) && idProducto.equals(p.getIdProducto()))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El servicio y producto indicados no estan en el catalogo."));
    }

    private String generarReferencia() {
        String ref;
        do {
            StringBuilder sb = new StringBuilder(LONGITUD_REFERENCIA);
            for (int i = 0; i < LONGITUD_REFERENCIA; i++) {
                sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
            }
            ref = sb.toString();
        } while (transaccionRepository.existsByReferenciaOperacion(ref));
        return ref;
    }

    private static TransaccionResponse toResponse(Transaccion tx) {
        return new TransaccionResponse(
                tx.getReferenciaOperacion(),
                tx.getEstado(),
                tx.getCuenta().getNumeroCuenta(),
                tx.getCuenta().getSaldo(),
                tx.getIdServicio(),
                tx.getIdProducto(),
                tx.getMonto(),
                tx.getNumAutorizacion(),
                tx.getCodigoProveedor(),
                tx.getFechaCreacion());
    }

    /** Datos derivados del producto para armar el payload. */
    private record DatosPago(String telefono, String referencia, boolean pagoServicio) {}
}
