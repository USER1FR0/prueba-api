package com.proyecto.servicios.service.postal;

public interface PostalCodeService {

    /**
     * Valida que el codigo postal exista realmente en el pais indicado (ISO 2 letras).
     * @return VALIDO si existe, NO_EXISTE si la API confirma que no existe,
     *         NO_VERIFICABLE si la API no esta disponible (no se bloquea el alta).
     */
    ResultadoValidacionCP validar(String codigoPostal, String paisIso);
}
