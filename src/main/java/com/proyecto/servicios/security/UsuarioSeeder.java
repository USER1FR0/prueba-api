package com.proyecto.servicios.security;

import com.proyecto.servicios.entity.auth.Usuario;
import com.proyecto.servicios.repositorys.auth.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Siembra un usuario de prueba al arranque SOLO si no existe ya ese correo.
 * El password se hashea con BCrypt; nunca se guarda en claro.
 * Controlado por auth.seed.* en application.properties.
 */
@Component
@Slf4j
public class UsuarioSeeder implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedEnabled;
    private final String seedCorreo;
    private final String seedPassword;

    public UsuarioSeeder(UsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder,
                         @Value("${auth.seed.enabled:false}") boolean seedEnabled,
                         @Value("${auth.seed.correo:}") String seedCorreo,
                         @Value("${auth.seed.password:}") String seedPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedEnabled = seedEnabled;
        this.seedCorreo = seedCorreo;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled || seedCorreo.isBlank() || seedPassword.isBlank()) {
            return;
        }
        if (usuarioRepository.existsByCorreo(seedCorreo)) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setCorreo(seedCorreo);
        usuario.setPasswordHash(passwordEncoder.encode(seedPassword));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        log.info("Usuario de prueba sembrado: {}", seedCorreo);
    }
}
