package com.proyecto.servicios.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Habilita el cacheo en memoria (ConcurrentMapCache por defecto) usado para
 * las validaciones de codigos postales, evitando golpear la API externa en
 * cada peticion y durante las pruebas de estres.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
