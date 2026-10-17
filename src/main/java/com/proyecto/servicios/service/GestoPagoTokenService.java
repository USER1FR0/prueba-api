package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.gestopago.GestoPagoToken;

import java.util.Optional;

public interface GestoPagoTokenService {

    /** Autentica contra GestoPago con las credenciales dadas y persiste el token resultante. */
    void renovarToken(Integer idDistribuidor, String codigoDispositivo, String password);

    Optional<GestoPagoToken> obtenerTokenActivo(Integer idDistribuidor, String codigoDispositivo);
}
