package com.proyecto.servicios.client.gestopago;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * Transacciones de GestoPago. Formulario urlencoded con {random, signed}; responde XML.
 * El read-timeout va por encima de 62 s (lo que GestoPago tarda antes de responder codigo 82).
 */
@FeignClient(name = "gestoPagoTx", url = "${gestopago.tx.url}")
public interface GestoPagoTxClient {

    @PostMapping(value = "/sistema/service/sendTx.do",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            headers = "Accept=text/xml, application/xml, */*")
    String sendTx(@RequestHeader("Authorization") String authorization,
                  @RequestHeader(value = "X-API-Key", required = false) String apiKey,
                  @RequestBody Map<String, ?> formulario);

    @PostMapping(value = "/sistema/service/confirmTx.do",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            headers = "Accept=text/xml, application/xml, */*")
    String confirmTx(@RequestHeader("Authorization") String authorization,
                     @RequestHeader(value = "X-API-Key", required = false) String apiKey,
                     @RequestBody Map<String, ?> formulario);
}
