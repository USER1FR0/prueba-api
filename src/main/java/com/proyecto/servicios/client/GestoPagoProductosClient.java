package com.proyecto.servicios.client;

import com.proyecto.servicios.config.GestoPagoProductosClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "gestoPagoProductos",
        url = "${gestopago.productos.url}",
        configuration = GestoPagoProductosClientConfig.class
)
public interface GestoPagoProductosClient {

    @GetMapping(value = "/sistema/service/getProductList.do", produces = MediaType.APPLICATION_XML_VALUE)
    String getProductList();
}