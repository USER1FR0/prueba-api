package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.auth.request.EstatusUsuarioRequest;
import com.proyecto.servicios.model.auth.request.UsuarioFiltroRequest;
import com.proyecto.servicios.model.auth.response.UsuarioResponse;
import com.proyecto.servicios.model.common.ApiResponse;
import com.proyecto.servicios.service.auth.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Consulta y cambio de estatus de usuarios de acceso. Se crean automaticamente al registrar un cliente.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Buscar usuarios por estatus",
            description = "Sin 'activo' devuelve todos; filtra por activos/inactivos. Nunca expone el password.")
    @PostMapping("/buscar")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> buscar(@RequestBody UsuarioFiltroRequest filtro) {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.filtrarPorActivo(filtro.getActivo())));
    }

    @Operation(summary = "Activar / inactivar usuario",
            description = "Cambia el estatus del usuario. El id va en el body.")
    @PutMapping("/estatus")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cambiarEstatus(@Valid @RequestBody EstatusUsuarioRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                usuarioService.cambiarEstatus(request.getId(), request.getActivo())));
    }
}
