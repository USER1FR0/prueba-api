package com.proyecto.servicios.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApi {

    private static final String ESQUEMA_JWT = "bearer-jwt";

    @Bean
    public OpenAPI openAPI() {
        Server servidor = new Server().url("/").description("Host actual");
        return new OpenAPI()
                .servers(List.of(servidor))
                .info(new Info()
                        .title("Onboarding de Clientes - API")
                        .version("v1")
                        .description("API REST para el onboarding de clientes persona fisica: "
                                + "alta con validaciones, cuentas, usuarios y autenticacion JWT. "
                                + "Todos los endpoints de negocio viven bajo el prefijo /api/v1 y requieren "
                                + "un token Bearer (obtenlo en /api/v1/auth/login y pulsa Authorize).")
                        .contact(new Contact().name("Equipo Onboarding")))
                // Define el esquema Bearer JWT y lo exige por defecto (boton Authorize en Swagger)
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Pega el token devuelto por /api/v1/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
