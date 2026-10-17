package com.proyecto.servicios.model.onboarding.request;

import com.proyecto.servicios.catalogo.EstatusCuenta;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/** Filtros combinables para buscar cuentas. */
@Getter
@Setter
@Schema(description = "Filtros combinables para buscar cuentas")
public class CuentaFiltroRequest {
    private Long clienteId;
    private EstatusCuenta estatus;
    private String numeroCuenta;
}
