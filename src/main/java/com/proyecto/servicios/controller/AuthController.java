package com.proyecto.servicios.controller;

import com.proyecto.servicios.exception.ErrorResponse;
import com.proyecto.servicios.model.auth.request.LoginRequest;
import com.proyecto.servicios.model.auth.response.LoginResponse;
import com.proyecto.servicios.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Endpoint aislado de login: emite un JWT a partir de correo y password. Aun no protege el resto de la API.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Iniciar sesion",
            description = "Valida las credenciales contra la tabla de usuarios y devuelve el token JWT, su tipo y el tiempo de expiracion.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales validas, token emitido",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Correo o password incorrectos / usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
