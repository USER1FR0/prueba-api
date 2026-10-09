package com.proyecto.servicios.controller;

import com.proyecto.servicios.exception.ErrorResponse;
import com.proyecto.servicios.model.auth.request.EstatusUsuarioRequest;
import com.proyecto.servicios.model.auth.response.UsuarioResponse;
import com.proyecto.servicios.service.auth.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Gestion de usuarios de acceso. Los usuarios se crean automaticamente al registrar un cliente; aqui solo se consultan por estatus y se activan/inactivan.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Filtrar usuarios por estatus",
            description = "Devuelve los usuarios segun su estatus. Sin el parametro 'activo' devuelve todos. Nunca expone el password.")
    @ApiResponse(responseCode = "200", description = "Listado de usuarios")
    @GetMapping("/filtro")
    public ResponseEntity<List<UsuarioResponse>> filtrar(
            @Parameter(description = "true=activos, false=inactivos; omitir para todos")
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(usuarioService.filtrarPorActivo(activo));
    }

    @Operation(summary = "Activar / inactivar usuario",
            description = "Cambia el estatus del usuario (true=activo, false=inactivo).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Body invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}/estatus")
    public ResponseEntity<UsuarioResponse> cambiarEstatus(@PathVariable Long id,
                                                          @Valid @RequestBody EstatusUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.cambiarEstatus(id, request.getActivo()));
    }
}
