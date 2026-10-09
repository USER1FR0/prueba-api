package com.proyecto.servicios.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Configuracion del cliente zipcodestack: inyecta el header apikey
 * desde la variable de entorno (nunca hardcodeado).
 */
public class ZipcodestackClientConfig {

    @Value("${postal.zipcodestack.api-key:}")
    private String apiKey;

    @Bean
    public RequestInterceptor zipcodestackApiKeyInterceptor() {
        return template -> {
            if (apiKey != null && !apiKey.isBlank()) {
                template.header("apikey", apiKey);
            }
        };
    }
}
