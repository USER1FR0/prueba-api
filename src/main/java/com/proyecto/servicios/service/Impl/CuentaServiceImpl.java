package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.catalogo.EstatusCuenta;
import com.proyecto.servicios.entity.onboarding.Cliente;
import com.proyecto.servicios.entity.onboarding.Cuenta;
import com.proyecto.servicios.exception.ClienteNoEncontradoException;
import com.proyecto.servicios.exception.CuentaNoEncontradaException;
import com.proyecto.servicios.exception.OperacionNoPermitidaException;
import com.proyecto.servicios.mapper.CuentaMapper;
import com.proyecto.servicios.model.onboarding.request.CuentaRequest;
import com.proyecto.servicios.model.onboarding.request.CuentaUpdateRequest;
import com.proyecto.servicios.model.onboarding.response.CuentaResponse;
import com.proyecto.servicios.model.onboarding.response.SaldoResponse;
import com.proyecto.servicios.repositorys.onboarding.ClienteRepository;
import com.proyecto.servicios.repositorys.onboarding.CuentaRepository;
import com.proyecto.servicios.service.CuentaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    public CuentaResponse crearCuentaParaCliente(Cliente cliente, BigDecimal saldoInicial) {
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new OperacionNoPermitidaException(
                    "No se puede crear una cuenta activa para un cliente inactivo");
        }
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

    @Override
    @Transactional
    public CuentaResponse crearCuenta(CuentaRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ClienteNoEncontradoException(
                        "No existe el cliente con id " + request.getClienteId()));
        return crearCuentaParaCliente(cliente, request.getSaldoInicial());
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponse obtenerPorNumero(String numeroCuenta) {
        return cuentaMapper.toResponse(obtenerEntidadPorNumero(numeroCuenta));
    }

    @Override
    @Transactional(readOnly = true)
    public Cuenta obtenerEntidadPorNumero(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(
                        "No existe la cuenta " + numeroCuenta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> consultarPorCliente(Long clienteId) {
        return cuentaMapper.toResponseList(cuentaRepository.findByClienteId(clienteId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> consultarPorEstatus(EstatusCuenta estatus) {
        return cuentaMapper.toResponseList(cuentaRepository.findByEstatus(estatus));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> consultarActivas() {
        return consultarPorEstatus(EstatusCuenta.ACTIVA);
    }

    @Override
    @Transactional(readOnly = true)
    public SaldoResponse consultarSaldo(String numeroCuenta) {
        Cuenta cuenta = obtenerEntidadPorNumero(numeroCuenta);
        return new SaldoResponse(cuenta.getNumeroCuenta(), cuenta.getSaldo());
    }

    @Override
    @Transactional
    public CuentaResponse actualizarParcial(String numeroCuenta, CuentaUpdateRequest request) {
        Cuenta cuenta = obtenerEntidadPorNumero(numeroCuenta);
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
        cuentas.forEach(cuenta -> cuenta.setEstatus(EstatusCuenta.INACTIVA));
        cuentaRepository.saveAll(cuentas);
        log.info("Se inactivaron {} cuentas del cliente {}", cuentas.size(), clienteId);
    }

    private String generarNumeroUnico() {
        for (int intento = 0; intento < MAX_INTENTOS_NUMERO; intento++) {
            String candidato = generarNumero();
            if (!cuentaRepository.existsByNumeroCuenta(candidato)) {
                return candidato;
            }
        }
        throw new OperacionNoPermitidaException(
                "No fue posible generar un numero de cuenta unico, intente nuevamente");
    }

    private String generarNumero() {
        StringBuilder sb = new StringBuilder(longitudNumeroCuenta);
        for (int i = 0; i < longitudNumeroCuenta; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(10));
        }
        return sb.toString();
    }
}
