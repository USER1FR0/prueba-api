package com.proyecto.servicios.mapper;

import com.proyecto.servicios.entity.onboarding.Cuenta;
import com.proyecto.servicios.model.onboarding.response.CuentaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CuentaMapper {

    @Mapping(target = "clienteId", source = "cliente.id")
    CuentaResponse toResponse(Cuenta cuenta);

    List<CuentaResponse> toResponseList(List<Cuenta> cuentas);
}
