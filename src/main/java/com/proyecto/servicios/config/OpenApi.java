package com.proyecto.servicios.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApi {

    @Bean
    public OpenAPI openAPI() {
        // Servidor RELATIVO: Swagger usa el host donde se sirve (local o produccion),
        // evitando que "Try it out" apunte siempre a localhost.
        Server servidor = new Server().url("/").description("Host actual");
        return new OpenAPI()
                .servers(List.of(servidor))
                .info(new Info()
                        .title("Onboarding de Clientes - API")
                        .version("v1")
                        .description("API REST para el onboarding de clientes persona fisica: "
                                + "alta con validaciones, cuentas, usuarios y autenticacion JWT. "
                                + "Todos los endpoints de negocio viven bajo el prefijo /api/v1.")
                        .contact(new Contact().name("Equipo Onboarding")));
    }
}
