package com.proyecto.servicios.job;

import com.proyecto.servicios.service.GestoPagoTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job periodico que renueva el token de GestoPago.
 * Mantiene las credenciales y el calendario fuera del service, como el resto de jobs.
 */
@Component
@Slf4j
public class GestoPagoTokenRefreshJob {

    private final GestoPagoTokenService gestoPagoTokenService;
    private final Integer idDistribuidor;
    private final String codigoDispositivo;
    private final String password;

    public GestoPagoTokenRefreshJob(GestoPagoTokenService gestoPagoTokenService,
                                    @Value("${gestopago.auth.id-distribuidor}") Integer idDistribuidor,
                                    @Value("${gestopago.auth.codigo-dispositivo}") String codigoDispositivo,
                                    @Value("${gestopago.auth.password}") String password) {
        this.gestoPagoTokenService = gestoPagoTokenService;
        this.idDistribuidor = idDistribuidor;
        this.codigoDispositivo = codigoDispositivo;
        this.password = password;
    }

    @Scheduled(fixedRateString = "${gestopago.auth.refresh-rate-ms:3600000}", initialDelay = 0)
    public void renovar() {
        try {
            gestoPagoTokenService.renovarToken(idDistribuidor, codigoDispositivo, password);
        } catch (Exception e) {
            log.error("[CRON] Error al renovar token GestoPago: {}", e.getMessage());
        }
    }
}
