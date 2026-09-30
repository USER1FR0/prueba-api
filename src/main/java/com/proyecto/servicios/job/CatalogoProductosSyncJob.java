package com.proyecto.servicios.job;

import com.proyecto.servicios.service.CatalogoProductosService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CatalogoProductosSyncJob {

    private final CatalogoProductosService catalogoProductosService;

    public CatalogoProductosSyncJob(CatalogoProductosService catalogoProductosService) {
        this.catalogoProductosService = catalogoProductosService;
    }

    @Scheduled(cron = "${gestopago.productos.sync-cron:0 0 6 * * *}")
    public void sincronizarCatalogo() {
        log.info("[CRON] Iniciando sincronizacion programada del catalogo de productos");
        try {
            long total = catalogoProductosService.sincronizar();
            log.info("[CRON] Sincronizacion programada finalizada: {} productos", total);
        } catch (Exception e) {
            log.error("[CRON] Error en la sincronizacion programada del catalogo: {}", e.getMessage());
        }
    }
}
