package com.proyecto.servicios.model.onboarding.response;

import com.proyecto.servicios.catalogo.EstatusCuenta;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CuentaResponse {
    private String numeroCuenta;
    private Long clienteId;
    private BigDecimal saldo;
    private EstatusCuenta estatus;
    private LocalDateTime fechaCreacion;
}
