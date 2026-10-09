package com.proyecto.servicios.service.auth.Impl;

import com.proyecto.servicios.entity.auth.Usuario;
import com.proyecto.servicios.exception.ContrasenaInvalidaException;
import com.proyecto.servicios.exception.CorreoDuplicadoException;
import com.proyecto.servicios.exception.UsuarioNoEncontradoException;
import com.proyecto.servicios.model.auth.response.UsuarioResponse;
import com.proyecto.servicios.repositorys.auth.UsuarioRepository;
import com.proyecto.servicios.service.auth.UsuarioService;
import com.proyecto.servicios.validation.PatronesValidacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private static final Pattern PASSWORD = Pattern.compile(PatronesValidacion.PASSWORD);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Usuario crearParaCliente(Long clienteId, String correo, String rawPassword) {
        validarPassword(rawPassword);
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new CorreoDuplicadoException("Ya existe un usuario con el correo " + correo);
        }
        Usuario usuario = new Usuario();
        usuario.setClienteId(clienteId);
        usuario.setCorreo(correo);
        usuario.setPasswordHash(passwordEncoder.encode(rawPassword));
        usuario.setActivo(true);
        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario de acceso creado para cliente {}", clienteId);
        return guardado;
    }

    @Override
    @Transactional
    public void inactivarPorCliente(Long clienteId) {
        usuarioRepository.findByClienteId(clienteId).ifPresent(u -> {
            u.setActivo(false);
            usuarioRepository.save(u);
            log.info("Usuario del cliente {} inactivado por baja logica", clienteId);
        });
    }

    @Override
    @Transactional
    public void sincronizarCorreo(Long clienteId, String nuevoCorreo) {
        usuarioRepository.findByClienteId(clienteId).ifPresent(u -> {
            if (!u.getCorreo().equalsIgnoreCase(nuevoCorreo)) {
                if (usuarioRepository.existsByCorreo(nuevoCorreo)) {
                    throw new CorreoDuplicadoException("Ya existe un usuario con el correo " + nuevoCorreo);
                }
                u.setCorreo(nuevoCorreo);
                usuarioRepository.save(u);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> filtrarPorActivo(Boolean activo) {
        List<Usuario> usuarios = (activo == null)
                ? usuarioRepository.findAll()
                : usuarioRepository.findByActivo(activo);
        return usuarios.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public UsuarioResponse cambiarEstatus(Long id, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("No existe el usuario con id " + id));
        usuario.setActivo(activo);
        return toResponse(usuarioRepository.save(usuario));
    }

    // ---------- Helpers ----------

    private void validarPassword(String rawPassword) {
        if (rawPassword == null || !PASSWORD.matcher(rawPassword).matches()) {
            throw new ContrasenaInvalidaException(
                    "El password debe tener minimo 8 caracteres, una mayuscula, una minuscula, un numero y un caracter especial");
        }
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getClienteId(), u.getCorreo(), u.getActivo(),
                u.getFechaCreacion(), u.getFechaActualizacion());
    }
}
