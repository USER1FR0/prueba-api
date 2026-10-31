package com.proyecto.servicios.client.gestopago;

import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.model.gestopago.SendTxRespuestaXml;
import com.proyecto.servicios.security.CifradoGestoPago;
import com.proyecto.servicios.service.GestoPagoTokenService;
import feign.FeignException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Pasos de red del pago: cifra el payload, adjunta el token vigente y envia {random, signed} a GestoPago.
 * NUNCA lanza excepciones: todo caso dudoso se devuelve como ResultadoProveedor para que la capa de
 * negocio decida (aplicar, devolver o conciliar). Asi se cumple "no asumir que timeout = no procesado".
 */
@Component
@Slf4j
public class GestoPagoTxGateway {

    private final GestoPagoTxClient txClient;
    private final GestoPagoTokenService tokenService;
    private final CifradoGestoPago cifrado;
    private final JAXBContext contextoXml;
    private final Integer idDistribuidor;
    private final String codigoDispositivo;
    private final String password;
    private final String apiKey;

    public GestoPagoTxGateway(GestoPagoTxClient txClient,
                              GestoPagoTokenService tokenService,
                              CifradoGestoPago cifrado,
                              @Value("${gestopago.auth.id-distribuidor}") Integer idDistribuidor,
                              @Value("${gestopago.auth.codigo-dispositivo}") String codigoDispositivo,
                              @Value("${gestopago.auth.password}") String password,
                              @Value("${gestopago.tx.api-key:}") String apiKey) throws JAXBException {
        this.txClient = txClient;
        this.tokenService = tokenService;
        this.cifrado = cifrado;
        this.idDistribuidor = idDistribuidor;
        this.codigoDispositivo = codigoDispositivo;
        this.password = password;
        this.apiKey = (apiKey == null || apiKey.isBlank()) ? null : apiKey;
        this.contextoXml = JAXBContext.newInstance(SendTxRespuestaXml.class);
    }

    /**
     * Resultado de la llamada al proveedor.
     * @param enviada false solo cuando la peticion nunca salio (sin token/cifrado): seguro NO se aplico.
     *                true sin xml = resultado desconocido (hay que conciliar).
     */
    public record ResultadoProveedor(SendTxRespuestaXml xml, String cuerpo, String error, boolean enviada) {
        static ResultadoProveedor respuesta(SendTxRespuestaXml xml, String cuerpo) {
            return new ResultadoProveedor(xml, cuerpo, null, true);
        }
        static ResultadoProveedor sinRespuesta(String cuerpo, String error) {
            return new ResultadoProveedor(null, cuerpo, error, true);
        }
        static ResultadoProveedor noEnviada(String error) {
            return new ResultadoProveedor(null, null, error, false);
        }
        public boolean tieneRespuesta() { return xml != null; }
        public String codigo() { return xml != null ? xml.codigo() : null; }
    }

    public ResultadoProveedor enviar(String payloadJson) {
        return ejecutar("sendTx", payloadJson,
                form -> txClient.sendTx(bearer(token()), apiKey, form),
                form -> txClient.sendTx(bearer(tokenRenovado()), apiKey, form));
    }

    public ResultadoProveedor confirmar(String payloadJson) {
        return ejecutar("confirmTx", payloadJson,
                form -> txClient.confirmTx(bearer(token()), apiKey, form),
                form -> txClient.confirmTx(bearer(tokenRenovado()), apiKey, form));
    }

    private ResultadoProveedor ejecutar(String metodo, String payloadJson,
                                        Function<Map<String, String>, String> llamada,
                                        Function<Map<String, String>, String> reintentoTokenNuevo) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("random", String.valueOf(System.currentTimeMillis()));
        try {
            form.put("signed", cifrado.cifrar(payloadJson));
        } catch (IllegalStateException ex) {
            log.error("GestoPago {} no enviado (cifrado): {}", metodo, ex.getMessage());
            return ResultadoProveedor.noEnviada(ex.getMessage());
        }
        try {
            String cuerpo;
            try {
                cuerpo = llamada.apply(form);
            } catch (FeignException.Unauthorized | FeignException.Forbidden ex) {
                log.warn("GestoPago {} rechazo el token ({}); se renueva y reintenta", metodo, ex.status());
                cuerpo = reintentoTokenNuevo.apply(form);
            }
            return interpretar(metodo, cuerpo);
        } catch (TokenNoDisponibleException ex) {
            log.error("GestoPago {} no enviado (sin token): {}", metodo, ex.getMessage());
            return ResultadoProveedor.noEnviada(ex.getMessage());
        } catch (FeignException ex) {
            log.error("GestoPago {} HTTP {}", metodo, ex.status());
            return ResultadoProveedor.sinRespuesta(null, "HTTP " + ex.status() + " de GestoPago");
        } catch (RuntimeException ex) {
            log.error("Sin respuesta de GestoPago {}: {}", metodo, ex.getClass().getSimpleName());
            return ResultadoProveedor.sinRespuesta(null, "Sin respuesta: " + ex.getClass().getSimpleName());
        }
    }

    private ResultadoProveedor interpretar(String metodo, String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            return ResultadoProveedor.sinRespuesta(cuerpo, "Respuesta vacia de GestoPago");
        }
        try {
            SendTxRespuestaXml xml = (SendTxRespuestaXml)
                    contextoXml.createUnmarshaller().unmarshal(new StringReader(cuerpo));
            log.info("GestoPago {} codigo={}", metodo, xml.codigo());
            return ResultadoProveedor.respuesta(xml, cuerpo);
        } catch (JAXBException ex) {
            log.error("XML de GestoPago {} ilegible: {}", metodo, ex.getMessage());
            return ResultadoProveedor.sinRespuesta(cuerpo, "Respuesta de GestoPago no reconocida");
        }
    }

    private String token() {
        return tokenService.obtenerTokenActivo(idDistribuidor, codigoDispositivo)
                .map(GestoPagoToken::getToken)
                .filter(t -> t != null && !t.isBlank())
                .orElseThrow(() -> new TokenNoDisponibleException("No hay token vigente de GestoPago"));
    }

    private String tokenRenovado() {
        tokenService.renovarToken(idDistribuidor, codigoDispositivo, password);
        return token();
    }

    private static String bearer(String token) {
        String t = token.trim();
        return t.startsWith("Bearer ") ? t : "Bearer " + t;
    }

    static class TokenNoDisponibleException extends RuntimeException {
        TokenNoDisponibleException(String m) { super(m); }
    }
}
