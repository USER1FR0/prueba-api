package com.proyecto.servicios.repositorys.onboarding;

import com.proyecto.servicios.entity.onboarding.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCurp(String curp);

    boolean existsByRfc(String rfc);

    boolean existsByCorreo(String correo);

    Optional<Cliente> findByCurp(String curp);

    Optional<Cliente> findByRfc(String rfc);

    Optional<Cliente> findByCorreo(String correo);

    List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    List<Cliente> findByApellidoPaternoContainingIgnoreCase(String apellidoPaterno);

    List<Cliente> findByApellidoMaternoContainingIgnoreCase(String apellidoMaterno);

    List<Cliente> findByActivoTrue();

    List<Cliente> findByFechaCreacionBetween(LocalDateTime desde, LocalDateTime hasta);
}
