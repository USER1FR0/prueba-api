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

    CuentaResponse obtenerPorNumero(String numeroCuenta);

    Cuenta obtenerEntidadPorNumero(String numeroCuenta);

    List<CuentaResponse> consultarPorCliente(Long clienteId);

    List<CuentaResponse> consultarPorEstatus(EstatusCuenta estatus);

    List<CuentaResponse> consultarActivas();

    SaldoResponse consultarSaldo(String numeroCuenta);

    CuentaResponse actualizarParcial(String numeroCuenta, CuentaUpdateRequest request);

    void inactivarCuentasDeCliente(Long clienteId);
}
