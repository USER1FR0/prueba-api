package com.proyecto.servicios.repositories.onboarding;

import com.proyecto.servicios.catalogo.EstatusCuenta;
import com.proyecto.servicios.entity.onboarding.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long>, JpaSpecificationExecutor<Cuenta> {

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    boolean existsByNumeroCuenta(String numeroCuenta);

    List<Cuenta> findByClienteId(Long clienteId);

    List<Cuenta> findByEstatus(EstatusCuenta estatus);

    List<Cuenta> findByClienteIdAndEstatus(Long clienteId, EstatusCuenta estatus);
}
