package com.proyecto.servicios.entity.onboarding;

import com.proyecto.servicios.catalogo.EstadoCivil;
import com.proyecto.servicios.catalogo.Sexo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Getter
@Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "segundo_nombre", length = 50)
    private String segundoNombre;

    @Column(name = "apellido_paterno", nullable = false, length = 50)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 50)
    private String apellidoMaterno;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "curp", nullable = false, length = 18, updatable = false)
    private String curp;

    @Column(name = "rfc", nullable = false, length = 13, updatable = false)
    private String rfc;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false, length = 10)
    private Sexo sexo;

    @Column(name = "nacionalidad", nullable = false, length = 50)
    private String nacionalidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", nullable = false, length = 15)
    private EstadoCivil estadoCivil;

    @Column(name = "correo", nullable = false, length = 100)
    private String correo;

    @Column(name = "telefono_movil", nullable = false, length = 10)
    private String telefonoMovil;

    @Column(name = "telefono_alterno", length = 10)
    private String telefonoAlterno;

    @Column(name = "ocupacion", nullable = false, length = 100)
    private String ocupacion;

    @Column(name = "empresa", nullable = false, length = 100)
    private String empresa;

    @Column(name = "ingreso_mensual", nullable = false, precision = 14, scale = 2)
    private BigDecimal ingresoMensual;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Domicilio domicilio;

    @PrePersist
    void onCreate() {
        LocalDateTime ahora = LocalDateTime.now();
        this.fechaCreacion = ahora;
        this.fechaActualizacion = ahora;
        if (this.activo == null) {
            this.activo = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
