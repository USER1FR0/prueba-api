package com.proyecto.servicios.service.auth.Impl;

import com.proyecto.servicios.entity.auth.Usuario;
import com.proyecto.servicios.exception.CredencialesInvalidasException;
import com.proyecto.servicios.model.auth.request.LoginRequest;
import com.proyecto.servicios.model.auth.response.LoginResponse;
import com.proyecto.servicios.repositorys.auth.UsuarioRepository;
import com.proyecto.servicios.security.JwtProvider;
import com.proyecto.servicios.service.auth.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String CREDENCIALES_INVALIDAS = "Correo o password incorrectos";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           JwtProvider jwtProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // Mismo mensaje para usuario inexistente o password incorrecto (no filtrar cual fallo).
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new CredencialesInvalidasException(CREDENCIALES_INVALIDAS));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new CredencialesInvalidasException("El usuario esta inactivo");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException(CREDENCIALES_INVALIDAS);
        }

        JwtProvider.TokenGenerado t = jwtProvider.generar(usuario.getCorreo());
        log.info("Login exitoso para {}", usuario.getCorreo());

        return new LoginResponse(
                usuario.getCorreo(),
                "Bearer",
                t.token(),
                t.emitidoEn(),
                t.expiraEn(),
                t.expiraEnMs() / 1000
        );
    }
}
