package com.proyecto.servicios.repositorys.auth;

import com.proyecto.servicios.entity.auth.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByClienteId(Long clienteId);
    boolean existsByCorreo(String correo);
    List<Usuario> findByActivo(Boolean activo);
}
