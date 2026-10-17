package com.proyecto.servicios.service;

import com.proyecto.servicios.model.onboarding.request.ClienteFiltroRequest;
import com.proyecto.servicios.model.onboarding.request.ClienteRequest;
import com.proyecto.servicios.model.onboarding.request.ClienteUpdateRequest;
import com.proyecto.servicios.model.onboarding.response.ClienteResponse;

import java.util.List;

public interface ClienteService {

    ClienteResponse registrar(ClienteRequest request);

    /** Busqueda unica con filtros combinables (ver ClienteSpecifications). */
    List<ClienteResponse> buscar(ClienteFiltroRequest filtro);

    ClienteResponse actualizarParcial(ClienteUpdateRequest request);

    void bajaLogica(Long id);
}
