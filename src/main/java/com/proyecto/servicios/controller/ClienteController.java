package com.proyecto.servicios.controller;

import com.proyecto.servicios.exception.ErrorResponse;
import com.proyecto.servicios.model.onboarding.request.ClienteRequest;
import com.proyecto.servicios.model.onboarding.request.ClienteUpdateRequest;
import com.proyecto.servicios.model.onboarding.response.ClienteResponse;
import com.proyecto.servicios.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Alta, consulta, actualizacion y baja logica de clientes persona fisica. El alta valida formato, unicidad y la existencia real del codigo postal.")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(summary = "Registrar cliente",
            description = "Da de alta un cliente y le crea automaticamente una cuenta. Valida CURP/RFC, unicidad (CURP, RFC, correo), mayoria de edad y que el codigo postal exista realmente para el pais (ISO).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o codigo postal inexistente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "CURP, RFC o correo ya registrados",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Object> registrar(@Valid @RequestBody ClienteRequest request) {
        return new ResponseEntity<>(clienteService.registrar(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar / listar clientes",
            description = "Sin parametros devuelve todos. Con parametros filtra por: curp, rfc, correo o numeroCuenta (exactos, devuelven un cliente); nombre, apellidoPaterno o apellidoMaterno (parcial); activo=true (solo activos); o rango desde+hasta por fecha de creacion. Se evalua el primer filtro presente en ese orden.")
    @ApiResponse(responseCode = "200", description = "Resultado de la busqueda")
    @ApiResponse(responseCode = "404", description = "No existe cliente para el filtro exacto indicado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping
    public ResponseEntity<Object> buscar(
            @Parameter(description = "Nombre (coincidencia parcial)") @RequestParam(required = false) String nombre,
            @Parameter(description = "Apellido paterno (parcial)") @RequestParam(required = false) String apellidoPaterno,
            @Parameter(description = "Apellido materno (parcial)") @RequestParam(required = false) String apellidoMaterno,
            @Parameter(description = "CURP exacta") @RequestParam(required = false) String curp,
            @Parameter(description = "RFC exacto") @RequestParam(required = false) String rfc,
            @Parameter(description = "Correo exacto") @RequestParam(required = false) String correo,
            @Parameter(description = "Numero de cuenta exacto") @RequestParam(required = false) String numeroCuenta,
            @Parameter(description = "Solo activos cuando es true") @RequestParam(required = false) Boolean activo,
            @Parameter(description = "Fecha inicial (yyyy-MM-dd), usar junto con hasta") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Fecha final (yyyy-MM-dd), usar junto con desde") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        if (curp != null) {
            return ResponseEntity.ok(clienteService.consultarPorCurp(curp));
        }
        if (rfc != null) {
            return ResponseEntity.ok(clienteService.consultarPorRfc(rfc));
        }
        if (correo != null) {
            return ResponseEntity.ok(clienteService.consultarPorCorreo(correo));
        }
        if (numeroCuenta != null) {
            return ResponseEntity.ok(clienteService.consultarPorNumeroCuenta(numeroCuenta));
        }
        if (nombre != null) {
            return ResponseEntity.ok(clienteService.buscarPorNombre(nombre));
        }
        if (apellidoPaterno != null) {
            return ResponseEntity.ok(clienteService.buscarPorApellidoPaterno(apellidoPaterno));
        }
        if (apellidoMaterno != null) {
            return ResponseEntity.ok(clienteService.buscarPorApellidoMaterno(apellidoMaterno));
        }
        if (Boolean.TRUE.equals(activo)) {
            return ResponseEntity.ok(clienteService.consultarActivos());
        }
        if (desde != null && hasta != null) {
            return ResponseEntity.ok(clienteService.consultarPorRangoFechas(desde, hasta));
        }
        return ResponseEntity.ok(clienteService.consultarTodos());
    }

    @Operation(summary = "Consultar cliente por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> consultarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.consultarPorId(id));
    }

    @Operation(summary = "Actualizar cliente (parcial)",
            description = "Actualiza solo los campos enviados. No permite cambiar CURP ni RFC. Si se envia domicilio, revalida el codigo postal contra la API.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o codigo postal inexistente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Correo ya usado por otro cliente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Object> actualizar(@PathVariable Long id,
                                             @Valid @RequestBody ClienteUpdateRequest request) {
        return ResponseEntity.ok(clienteService.actualizarParcial(id, request));
    }

    @Operation(summary = "Baja logica del cliente",
            description = "Marca al cliente como inactivo e inactiva sus cuentas. No borra datos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente dado de baja"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> bajaLogica(@PathVariable Long id) {
        clienteService.bajaLogica(id);
        return ResponseEntity.noContent().build();
    }
}
