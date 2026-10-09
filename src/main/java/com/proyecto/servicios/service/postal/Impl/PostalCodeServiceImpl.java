package com.proyecto.servicios.service.postal.Impl;

import com.proyecto.servicios.client.postal.ZipcodestackClient;
import com.proyecto.servicios.client.postal.ZipcodestackResponse;
import com.proyecto.servicios.client.postal.ZippopotamClient;
import com.proyecto.servicios.client.postal.ZippopotamResponse;
import com.proyecto.servicios.service.postal.PostalCodeService;
import com.proyecto.servicios.service.postal.ResultadoValidacionCP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Valida codigos postales reales usando una API internacional.
 *
 * Estrategia:
 *  1. Si hay API key de zipcodestack configurada, se consulta esa API (cubre todos los paises).
 *  2. Si no hay key o zipcodestack falla, se usa zippopotam.us como respaldo (sin key).
 *  3. Si ninguna API responde, se devuelve NO_VERIFICABLE (fail-open): no se bloquea el alta
 *     por una caida externa, pero se deja registro en el log.
 *
 * Los resultados se cachean por (cp, pais) para no golpear la API en pruebas de estres.
 */
@Service
@Slf4j
public class PostalCodeServiceImpl implements PostalCodeService {

    private final ZipcodestackClient zipcodestackClient;
    private final ZippopotamClient zippopotamClient;
    private final String zipcodestackApiKey;

    public PostalCodeServiceImpl(ZipcodestackClient zipcodestackClient,
                                 ZippopotamClient zippopotamClient,
                                 @Value("${postal.zipcodestack.api-key:}") String zipcodestackApiKey) {
        this.zipcodestackClient = zipcodestackClient;
        this.zippopotamClient = zippopotamClient;
        this.zipcodestackApiKey = zipcodestackApiKey;
    }

    @Override
    @Cacheable(value = "codigosPostales", key = "#paisIso + '|' + #codigoPostal", unless = "#result == T(com.proyecto.servicios.service.postal.ResultadoValidacionCP).NO_VERIFICABLE")
    public ResultadoValidacionCP validar(String codigoPostal, String paisIso) {
        if (codigoPostal == null || codigoPostal.isBlank() || paisIso == null || paisIso.isBlank()) {
            return ResultadoValidacionCP.NO_VERIFICABLE;
        }
        String cp = codigoPostal.trim();
        String pais = paisIso.trim().toUpperCase();

        boolean hayKey = zipcodestackApiKey != null && !zipcodestackApiKey.isBlank();

        if (hayKey) {
            ResultadoValidacionCP r = consultarZipcodestack(cp, pais);
            if (r != ResultadoValidacionCP.NO_VERIFICABLE) {
                return r;
            }
            // zipcodestack no respondio -> intentar respaldo
        }

        return consultarZippopotam(cp, pais);
    }

    private ResultadoValidacionCP consultarZipcodestack(String cp, String pais) {
        try {
            ZipcodestackResponse resp = zipcodestackClient.buscar(cp, pais);
            Map<String, List<ZipcodestackResponse.PostalLocation>> results =
                    resp == null ? null : resp.getResults();
            if (results == null) {
                return ResultadoValidacionCP.NO_VERIFICABLE;
            }
            List<ZipcodestackResponse.PostalLocation> loc = results.get(cp);
            boolean existe = loc != null && !loc.isEmpty();
            return existe ? ResultadoValidacionCP.VALIDO : ResultadoValidacionCP.NO_EXISTE;
        } catch (Exception e) {
            log.warn("zipcodestack no disponible para CP {} ({}): {}", cp, pais, e.getMessage());
            return ResultadoValidacionCP.NO_VERIFICABLE;
        }
    }

    private ResultadoValidacionCP consultarZippopotam(String cp, String pais) {
        try {
            ZippopotamResponse resp = zippopotamClient.buscar(pais.toLowerCase(), cp);
            boolean existe = resp != null && resp.getPlaces() != null && !resp.getPlaces().isEmpty();
            return existe ? ResultadoValidacionCP.VALIDO : ResultadoValidacionCP.NO_EXISTE;
        } catch (feign.FeignException.NotFound nf) {
            // 404 = la API confirma que el CP no existe en ese pais
            return ResultadoValidacionCP.NO_EXISTE;
        } catch (Exception e) {
            log.warn("zippopotam no disponible para CP {} ({}): {}", cp, pais, e.getMessage());
            return ResultadoValidacionCP.NO_VERIFICABLE;
        }
    }
}
