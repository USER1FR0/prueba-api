package com.proyecto.servicios.entity.onboarding;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "domicilios")
@Getter
@Setter
public class Domicilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calle", nullable = false, length = 100)
    private String calle;

    @Column(name = "numero_exterior", nullable = false, length = 10)
    private String numeroExterior;

    @Column(name = "numero_interior", length = 10)
    private String numeroInterior;

    @Column(name = "colonia", nullable = false, length = 100)
    private String colonia;

    @Column(name = "municipio", nullable = false, length = 100)
    private String municipio;

    @Column(name = "estado", length = 100)
    private String estado;

    @Column(name = "codigo_postal", nullable = false, length = 10)
    private String codigoPostal;

    @Column(name = "pais", nullable = false, length = 2)
    private String pais;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;
}
