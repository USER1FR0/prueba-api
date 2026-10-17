package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.common.ApiResponse;
import com.proyecto.servicios.model.onboarding.request.CuentaFiltroRequest;
import com.proyecto.servicios.model.onboarding.request.CuentaRequest;
import com.proyecto.servicios.model.onboarding.request.CuentaUpdateRequest;
import com.proyecto.servicios.model.onboarding.request.NumeroCuentaRequest;
import com.proyecto.servicios.model.onboarding.response.CuentaResponse;
import com.proyecto.servicios.model.onboarding.response.SaldoResponse;
import com.proyecto.servicios.service.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cuentas")
@Tag(name = "Cuentas", description = "Creacion, consulta, saldo y cambio de estatus de cuentas. Identificadores y filtros en el body.")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @Operation(summary = "Crear cuenta",
            description = "Crea una cuenta para un cliente activo. 201 exito; 400 saldo negativo; 404 cliente; 409 cliente inactivo.")
    @PostMapping
    public ResponseEntity<ApiResponse<CuentaResponse>> crear(@Valid @RequestBody CuentaRequest request) {
        return new ResponseEntity<>(ApiResponse.ok(cuentaService.crearCuenta(request)), HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar cuentas (filtros combinables)",
            description = "Sin filtros devuelve las activas; combina numeroCuenta, clienteId y/o estatus.")
    @PostMapping("/buscar")
    public ResponseEntity<ApiResponse<List<CuentaResponse>>> buscar(@RequestBody CuentaFiltroRequest filtro) {
        return ResponseEntity.ok(ApiResponse.ok(
                cuentaService.buscar(filtro.getClienteId(), filtro.getEstatus(), filtro.getNumeroCuenta())));
    }

    @Operation(summary = "Consultar saldo", description = "Devuelve el saldo de la cuenta indicada en el body.")
    @PostMapping("/saldo")
    public ResponseEntity<ApiResponse<SaldoResponse>> saldo(@Valid @RequestBody NumeroCuentaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(cuentaService.consultarSaldo(request.getNumeroCuenta())));
    }

    @Operation(summary = "Actualizar estatus de cuenta",
            description = "Cambia el estatus (ACTIVA/INACTIVA). El numero de cuenta va en el body.")
    @PatchMapping
    public ResponseEntity<ApiResponse<CuentaResponse>> actualizar(@Valid @RequestBody CuentaUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                cuentaService.actualizarParcial(request.getNumeroCuenta(), request)));
    }
}
