package com.proyecto.servicios.service;

import com.proyecto.servicios.model.onboarding.request.CuentaFiltroRequest;
import com.proyecto.servicios.model.onboarding.request.CuentaRequest;
import com.proyecto.servicios.model.onboarding.request.CuentaUpdateRequest;
import com.proyecto.servicios.model.onboarding.response.CuentaResponse;
import com.proyecto.servicios.model.onboarding.response.SaldoResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CuentaService {

    /** Crea la cuenta inicial del cliente recien dado de alta (saldo opcional). */
    CuentaResponse crearCuentaInicialParaCliente(Long clienteId, BigDecimal saldoInicial);

    /** Crea una cuenta a partir de un request de alta. */
    CuentaResponse crearCuenta(CuentaRequest request);

    /** Busqueda combinable con paginacion. Sin filtros, devuelve las activas. */
    List<CuentaResponse> buscar(CuentaFiltroRequest filtro);

    /** Lista las cuentas de un cliente (uso interno para armar el ClienteResponse). */
    List<CuentaResponse> consultarPorCliente(Long clienteId);

    /** Consulta el saldo de una cuenta por su numero. */
    SaldoResponse consultarSaldo(String numeroCuenta);

    /** Actualiza el estatus de una cuenta (identificada por numero en el body). */
    CuentaResponse actualizarParcial(String numeroCuenta, CuentaUpdateRequest request);

    /** Inactiva todas las cuentas del cliente (cascada de baja logica). */
    void inactivarCuentasDeCliente(Long clienteId);
}
