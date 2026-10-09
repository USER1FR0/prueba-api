package com.proyecto.servicios.mapper;

import com.proyecto.servicios.entity.onboarding.Cliente;
import com.proyecto.servicios.entity.onboarding.Domicilio;
import com.proyecto.servicios.model.onboarding.request.ClienteRequest;
import com.proyecto.servicios.model.onboarding.request.ClienteUpdateRequest;
import com.proyecto.servicios.model.onboarding.request.DomicilioRequest;
import com.proyecto.servicios.model.onboarding.response.ClienteResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "domicilio", source = "domicilio")
    Cliente toEntity(ClienteRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    Domicilio toDomicilioEntity(DomicilioRequest request);

    @Mapping(target = "cuentas", ignore = true)
    ClienteResponse toResponse(Cliente cliente);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "curp", ignore = true)
    @Mapping(target = "rfc", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "domicilio", ignore = true)
    void actualizarCliente(ClienteUpdateRequest request, @MappingTarget Cliente cliente);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    void actualizarDomicilio(DomicilioRequest request, @MappingTarget Domicilio domicilio);
}
