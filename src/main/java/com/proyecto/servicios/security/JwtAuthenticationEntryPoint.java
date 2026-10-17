package com.proyecto.servicios.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.model.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Devuelve 401 con el mismo envelope ApiResponse que el resto de la API
 * cuando una peticion llega sin token (o con token invalido) a un endpoint
 * protegido.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<List<String>> body = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "No autorizado",
                List.of("Token ausente, invalido o expirado"));
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
