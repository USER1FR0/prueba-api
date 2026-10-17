package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.common.ApiResponse;
import com.proyecto.servicios.model.onboarding.request.ClienteFiltroRequest;
import com.proyecto.servicios.model.onboarding.request.ClienteRequest;
import com.proyecto.servicios.model.onboarding.request.ClienteUpdateRequest;
import com.proyecto.servicios.model.onboarding.request.IdRequest;
import com.proyecto.servicios.model.onboarding.response.ClienteResponse;
import com.proyecto.servicios.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Alta, consulta, actualizacion y baja logica de clientes. Los identificadores y filtros viajan en el body (sin path ni query params).")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(summary = "Registrar cliente",
            description = "Da de alta un cliente y crea automaticamente su cuenta y su usuario de acceso. 201 exito; 400 datos/CP/password; 409 CURP/RFC/correo duplicado.")
    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponse>> registrar(@Valid @RequestBody ClienteRequest request) {
        return new ResponseEntity<>(ApiResponse.ok(clienteService.registrar(request)), HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar clientes (filtros combinables)",
            description = "Endpoint unico de consulta. Sin filtros devuelve todos; con filtros los combina (id, nombre, apellidos, curp, rfc, correo, numeroCuenta, activo, rango de fechas, pagina y tamanio).")
    @PostMapping("/buscar")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> buscar(@RequestBody ClienteFiltroRequest filtro) {
        return ResponseEntity.ok(ApiResponse.ok(clienteService.buscar(filtro)));
    }

    @Operation(summary = "Actualizar cliente (parcial)",
            description = "Actualiza solo los campos enviados. El id va en el body. CURP, RFC y numero de cuenta son inmutables.")
    @PatchMapping
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizar(@Valid @RequestBody ClienteUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(clienteService.actualizarParcial(request)));
    }

    @Operation(summary = "Baja logica del cliente",
            description = "Marca al cliente como inactivo e inactiva sus cuentas y su usuario. El id va en el body.")
    @PatchMapping("/baja")
    public ResponseEntity<ApiResponse<Void>> bajaLogica(@Valid @RequestBody IdRequest request) {
        clienteService.bajaLogica(request.getId());
        return ResponseEntity.ok(new ApiResponse<>(0, "Cliente dado de baja", null));
    }
}
