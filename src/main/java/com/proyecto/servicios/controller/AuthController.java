package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.auth.request.LoginRequest;
import com.proyecto.servicios.model.auth.response.LoginResponse;
import com.proyecto.servicios.model.common.ApiResponse;
import com.proyecto.servicios.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Login: emite un JWT a partir de correo y password.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Iniciar sesion",
            description = "Valida credenciales y devuelve el token JWT. 200 exito; 400 datos; 401 credenciales o usuario inactivo.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }
}
