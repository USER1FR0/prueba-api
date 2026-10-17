package com.proyecto.servicios.service.impl;

import com.proyecto.servicios.catalogo.EstatusCuenta;
import com.proyecto.servicios.entity.onboarding.Cliente;
import com.proyecto.servicios.entity.onboarding.Cuenta;
import com.proyecto.servicios.exception.ClienteNoEncontradoException;
import com.proyecto.servicios.exception.CuentaNoEncontradaException;
import com.proyecto.servicios.exception.OperacionNoPermitidaException;
import com.proyecto.servicios.mapper.CuentaMapper;
import com.proyecto.servicios.model.onboarding.request.CuentaFiltroRequest;
import com.proyecto.servicios.model.onboarding.request.CuentaRequest;
import com.proyecto.servicios.model.onboarding.request.CuentaUpdateRequest;
import com.proyecto.servicios.model.onboarding.response.CuentaResponse;
import com.proyecto.servicios.model.onboarding.response.SaldoResponse;
import com.proyecto.servicios.repositories.onboarding.ClienteRepository;
import com.proyecto.servicios.repositories.onboarding.CuentaRepository;
import com.proyecto.servicios.repositories.onboarding.CuentaSpecifications;
import com.proyecto.servicios.repositories.onboarding.PaginacionFiltro;
import com.proyecto.servicios.service.CuentaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class CuentaServiceImpl implements CuentaService {

    private static final int MAX_INTENTOS_NUMERO = 20;

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final CuentaMapper cuentaMapper;
    private final BigDecimal saldoInicialPorDefecto;
    private final int longitudNumeroCuenta;

    public CuentaServiceImpl(CuentaRepository cuentaRepository,
                             ClienteRepository clienteRepository,
                             CuentaMapper cuentaMapper,
                             @Value("${onboarding.cuenta.saldo-inicial}") BigDecimal saldoInicialPorDefecto,
                             @Value("${onboarding.cuenta.longitud-numero}") int longitudNumeroCuenta) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.cuentaMapper = cuentaMapper;
        this.saldoInicialPorDefecto = saldoInicialPorDefecto;
        this.longitudNumeroCuenta = longitudNumeroCuenta;
    }

    @Override
    @Transactional
    public CuentaResponse crearCuentaInicialParaCliente(Long clienteId, BigDecimal saldoInicial) {
        return crear(buscarClienteActivo(clienteId), saldoInicial);
    }

    @Override
    @Transactional
    public CuentaResponse crearCuenta(CuentaRequest request) {
        return crear(buscarClienteActivo(request.getClienteId()), request.getSaldoInicial());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> consultarPorCliente(Long clienteId) {
        return cuentaMapper.toResponseList(cuentaRepository.findByClienteId(clienteId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> buscar(CuentaFiltroRequest filtro) {
        Pageable pageable = PaginacionFiltro.de(filtro.getPage(), filtro.getSize());
        List<Cuenta> cuentas = sinFiltrosEspecificos(filtro)
                ? cuentaRepository.findAll(CuentaSpecifications.desde(filtroPorDefecto()), pageable).getContent()
                : cuentaRepository.findAll(CuentaSpecifications.desde(filtro), pageable).getContent();
        return cuentaMapper.toResponseList(cuentas);
    }

    @Override
    @Transactional(readOnly = true)
    public SaldoResponse consultarSaldo(String numeroCuenta) {
        Cuenta cuenta = buscarPorNumero(numeroCuenta);
        return new SaldoResponse(cuenta.getNumeroCuenta(), cuenta.getSaldo());
    }

    @Override
    @Transactional
    public CuentaResponse actualizarParcial(String numeroCuenta, CuentaUpdateRequest request) {
        Cuenta cuenta = buscarPorNumero(numeroCuenta);
        if (request.getEstatus() == EstatusCuenta.ACTIVA
                && !Boolean.TRUE.equals(cuenta.getCliente().getActivo())) {
            throw new OperacionNoPermitidaException(
                    "No se puede activar una cuenta de un cliente inactivo");
        }
        cuenta.setEstatus(request.getEstatus());
        return cuentaMapper.toResponse(cuentaRepository.save(cuenta));
    }

    @Override
    @Transactional
    public void inactivarCuentasDeCliente(Long clienteId) {
        List<Cuenta> cuentas = cuentaRepository.findByClienteId(clienteId);
        cuentas.forEach(c -> c.setEstatus(EstatusCuenta.INACTIVA));
        cuentaRepository.saveAll(cuentas);
        log.info("Se inactivaron {} cuentas del cliente {}", cuentas.size(), clienteId);
    }

    // ---------- Helpers ----------

    private CuentaResponse crear(Cliente cliente, BigDecimal saldoInicial) {
        BigDecimal saldo = (saldoInicial != null) ? saldoInicial : saldoInicialPorDefecto;
        if (saldo.signum() < 0) {
            throw new OperacionNoPermitidaException("El saldo inicial no puede ser negativo");
        }

        Cuenta cuenta = new Cuenta();
        cuenta.setCliente(cliente);
        cuenta.setNumeroCuenta(generarNumeroUnico());
        cuenta.setSaldo(saldo);
        cuenta.setEstatus(EstatusCuenta.ACTIVA);

        Cuenta guardada = cuentaRepository.save(cuenta);
        log.info("Cuenta {} creada para el cliente {}", guardada.getNumeroCuenta(), cliente.getId());
        return cuentaMapper.toResponse(guardada);
    }

    private Cliente buscarClienteActivo(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException(
                        "No existe el cliente con id " + clienteId));
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new OperacionNoPermitidaException(
                    "No se puede crear una cuenta activa para un cliente inactivo");
        }
        return cliente;
    }

    private Cuenta buscarPorNumero(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(
                        "No existe la cuenta " + numeroCuenta));
    }

    private boolean sinFiltrosEspecificos(CuentaFiltroRequest f) {
        return f.getClienteId() == null
                && f.getEstatus() == null
                && (f.getNumeroCuenta() == null || f.getNumeroCuenta().isBlank());
    }

    private CuentaFiltroRequest filtroPorDefecto() {
        CuentaFiltroRequest f = new CuentaFiltroRequest();
        f.setEstatus(EstatusCuenta.ACTIVA);
        return f;
    }

    private String generarNumeroUnico() {
        for (int intento = 0; intento < MAX_INTENTOS_NUMERO; intento++) {
            String candidato = generarNumero();
            if (!cuentaRepository.existsByNumeroCuenta(candidato)) {
                return candidato;
            }
        }
        throw new IllegalStateException(
                "No fue posible generar un numero de cuenta unico tras "
                        + MAX_INTENTOS_NUMERO + " intentos");
    }

    private String generarNumero() {
        StringBuilder sb = new StringBuilder(longitudNumeroCuenta);
        for (int i = 0; i < longitudNumeroCuenta; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(10));
        }
        return sb.toString();
    }
}
