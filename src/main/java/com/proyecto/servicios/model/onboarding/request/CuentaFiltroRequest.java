package com.proyecto.servicios.model.onboarding.request;

import com.proyecto.servicios.catalogo.EstatusCuenta;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/** Filtros combinables para buscar cuentas (incluye paginacion). */
@Getter
@Setter
@Schema(description = "Filtros combinables para buscar cuentas")
public class CuentaFiltroRequest {
    private Long clienteId;
    private EstatusCuenta estatus;
    private String numeroCuenta;

    @Schema(description = "Numero de pagina (0-based). Por defecto 0.", example = "0")
    private Integer page;

    @Schema(description = "Tamanio de pagina. Por defecto 50, maximo 200.", example = "50")
    private Integer size;
}
