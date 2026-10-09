package com.proyecto.servicios.config;

import com.proyecto.servicios.validation.TrimStringDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /** Aplica el trim a TODOS los String entrantes (blindaje de entradas). */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer trimStringsCustomizer() {
        return builder -> builder.deserializerByType(String.class, new TrimStringDeserializer());
    }
}
