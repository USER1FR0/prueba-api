package com.proyecto.servicios.controller;

import com.proyecto.servicios.catalogo.EstatusCuenta;
import com.proyecto.servicios.exception.ErrorResponse;
import com.proyecto.servicios.model.onboarding.request.CuentaRequest;
import com.proyecto.servicios.model.onboarding.request.CuentaUpdateRequest;
import com.proyecto.servicios.model.onboarding.response.CuentaResponse;
import com.proyecto.servicios.model.onboarding.response.SaldoResponse;
import com.proyecto.servicios.service.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cuentas")
@Tag(name = "Cuentas", description = "Gestion de cuentas de los clientes: creacion, consulta, saldo y cambio de estatus. El numero de cuenta se genera automaticamente.")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @Operation(summary = "Crear cuenta",
            description = "Crea una cuenta para un cliente activo. El saldo inicial es opcional (default configurado). El numero de cuenta se autogenera y es unico.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada",
                    content = @Content(schema = @Schema(implementation = CuentaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ej. saldo negativo)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El cliente esta inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Object> crear(@Valid @RequestBody CuentaRequest request) {
        return new ResponseEntity<>(cuentaService.crearCuenta(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Listar / filtrar cuentas",
            description = "Sin parametros devuelve las cuentas activas. Con clienteId filtra por cliente; con estatus filtra por ACTIVA o INACTIVA.")
    @ApiResponse(responseCode = "200", description = "Resultado de la busqueda")
    @GetMapping
    public ResponseEntity<Object> buscar(
            @Parameter(description = "Id del cliente") @RequestParam(required = false) Long clienteId,
            @Parameter(description = "Estatus de la cuenta") @RequestParam(required = false) EstatusCuenta estatus) {
        if (clienteId != null) {
            return ResponseEntity.ok(cuentaService.consultarPorCliente(clienteId));
        }
        if (estatus != null) {
            return ResponseEntity.ok(cuentaService.consultarPorEstatus(estatus));
        }
        return ResponseEntity.ok(cuentaService.consultarActivas());
    }

    @Operation(summary = "Consultar cuenta por numero")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada",
                    content = @Content(schema = @Schema(implementation = CuentaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<Object> consultarPorNumero(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.obtenerPorNumero(numeroCuenta));
    }

    @Operation(summary = "Consultar saldo de la cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo de la cuenta",
                    content = @Content(schema = @Schema(implementation = SaldoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{numeroCuenta}/saldo")
    public ResponseEntity<Object> consultarSaldo(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.consultarSaldo(numeroCuenta));
    }

    @Operation(summary = "Actualizar estatus de la cuenta",
            description = "Cambia el estatus de la cuenta a ACTIVA o INACTIVA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta actualizada",
                    content = @Content(schema = @Schema(implementation = CuentaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estatus invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{numeroCuenta}")
    public ResponseEntity<Object> actualizar(@PathVariable String numeroCuenta,
                                             @Valid @RequestBody CuentaUpdateRequest request) {
        return ResponseEntity.ok(cuentaService.actualizarParcial(numeroCuenta, request));
    }
}
