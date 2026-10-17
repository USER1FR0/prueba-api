package com.proyecto.servicios.model.onboarding.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Criterios de busqueda de clientes. Todos opcionales y COMBINABLES:
 * se aplican solo los que vengan presentes. 'page' y 'size' controlan la paginacion.
 */
@Getter
@Setter
@Schema(description = "Filtros combinables para buscar clientes")
public class ClienteFiltroRequest {
    private Long id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String curp;
    private String rfc;
    private String correo;
    private String numeroCuenta;
    private Boolean activo;
    private LocalDate desde;
    private LocalDate hasta;

    @Schema(description = "Numero de pagina (0-based). Por defecto 0.", example = "0")
    private Integer page;

    @Schema(description = "Tamanio de pagina. Por defecto 50, maximo 200.", example = "50")
    private Integer size;
}
