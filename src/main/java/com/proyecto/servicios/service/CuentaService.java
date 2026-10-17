package com.proyecto.servicios.service;

import com.proyecto.servicios.catalogo.EstatusCuenta;
import com.proyecto.servicios.entity.onboarding.Cliente;
import com.proyecto.servicios.entity.onboarding.Cuenta;
import com.proyecto.servicios.model.onboarding.request.CuentaRequest;
import com.proyecto.servicios.model.onboarding.request.CuentaUpdateRequest;
import com.proyecto.servicios.model.onboarding.response.CuentaResponse;
import com.proyecto.servicios.model.onboarding.response.SaldoResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CuentaService {

    CuentaResponse crearCuentaParaCliente(Cliente cliente, BigDecimal saldoInicial);

    CuentaResponse crearCuenta(CuentaRequest request);

    /** Busqueda combinable: numeroCuenta, clienteId y/o estatus (sin filtros, devuelve activas). */
    List<CuentaResponse> buscar(Long clienteId, EstatusCuenta estatus, String numeroCuenta);

    List<CuentaResponse> consultarPorCliente(Long clienteId);

    SaldoResponse consultarSaldo(String numeroCuenta);

    CuentaResponse actualizarParcial(String numeroCuenta, CuentaUpdateRequest request);

    void inactivarCuentasDeCliente(Long clienteId);

    Cuenta obtenerEntidadPorNumero(String numeroCuenta);
}
