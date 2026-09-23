package com.proyecto.servicios.config;

import feign.Request;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;
import feign.codec.ErrorDecoder;

public class GestoPagoProductosClientConfig {

    @Value("${gestopago.productos.token}")
    private String token;

    @Value("${gestopago.productos.connect-timeout-ms}")
    private long connectTimeoutMs;

    @Value("${gestopago.productos.read-timeout-ms}")
    private long readTimeoutMs;

    @Bean
    public RequestInterceptor gestoPagoProductosAuthInterceptor() {
        return requestTemplate -> requestTemplate.header("Authorization", "Bearer " + token);
    }

    @Bean
    public Request.Options gestoPagoProductosOptions() {
        return new Request.Options(
                connectTimeoutMs, TimeUnit.MILLISECONDS,
                readTimeoutMs, TimeUnit.MILLISECONDS,
                true);
    }

    @Bean
    public ErrorDecoder gestoPagoProductosErrorDecoder() {
        return new GestoPagoProductosErrorDecoder();
    }
}