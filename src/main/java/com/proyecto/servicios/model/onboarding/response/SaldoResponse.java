package com.proyecto.servicios.model.onboarding.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SaldoResponse {
    private String numeroCuenta;
    private BigDecimal saldo;

    public SaldoResponse(String numeroCuenta, BigDecimal saldo) {
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
    }
}
