package com.proyecto.servicios.model.onboarding.response;

import com.proyecto.servicios.catalogo.EstadoCivil;
import com.proyecto.servicios.catalogo.Sexo;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ClienteResponse {
    private Long id;
    private String nombre;
    private String segundoNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private LocalDate fechaNacimiento;
    private String curp;
    private String rfc;
    private Sexo sexo;
    private String nacionalidad;
    private EstadoCivil estadoCivil;
    private String correo;
    private String telefonoMovil;
    private String telefonoAlterno;
    private String ocupacion;
    private String empresa;
    private BigDecimal ingresoMensual;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private DomicilioResponse domicilio;
    private List<CuentaResponse> cuentas;
}
