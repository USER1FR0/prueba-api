package com.proyecto.servicios.service;

import com.proyecto.servicios.model.onboarding.request.ClienteRequest;
import com.proyecto.servicios.model.onboarding.request.ClienteUpdateRequest;
import com.proyecto.servicios.model.onboarding.response.ClienteResponse;

import java.time.LocalDate;
import java.util.List;

public interface ClienteService {

    ClienteResponse registrar(ClienteRequest request);

    List<ClienteResponse> consultarTodos();

    ClienteResponse consultarPorId(Long id);

    ClienteResponse consultarPorCurp(String curp);

    ClienteResponse consultarPorRfc(String rfc);

    ClienteResponse consultarPorCorreo(String correo);

    ClienteResponse consultarPorNumeroCuenta(String numeroCuenta);

    List<ClienteResponse> buscarPorNombre(String nombre);

    List<ClienteResponse> buscarPorApellidoPaterno(String apellidoPaterno);

    List<ClienteResponse> buscarPorApellidoMaterno(String apellidoMaterno);

    List<ClienteResponse> consultarActivos();

    List<ClienteResponse> consultarPorRangoFechas(LocalDate desde, LocalDate hasta);

    ClienteResponse actualizarParcial(Long id, ClienteUpdateRequest request);

    void bajaLogica(Long id);
}
