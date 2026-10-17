package com.proyecto.servicios.security;

import com.proyecto.servicios.repositories.auth.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Lee el header Authorization: Bearer <token>, valida el JWT con JwtProvider y
 * confirma que el usuario siga EXISTIENDO y ACTIVO antes de autenticar. Asi un
 * token de un usuario dado de baja deja de servir aunque no haya expirado.
 * Si falta el header, deja pasar sin autenticar (las rutas protegidas responden
 * 401 via el EntryPoint). Token invalido o usuario inactivo -> 401 con ApiResponse.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIJO = "Bearer ";

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint entryPoint;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtProvider jwtProvider,
                                   JwtAuthenticationEntryPoint entryPoint,
                                   UsuarioRepository usuarioRepository) {
        this.jwtProvider = jwtProvider;
        this.entryPoint = entryPoint;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extraerToken(request);
        if (token != null) {
            try {
                String correo = jwtProvider.validarYObtenerCorreo(token);
                if (!usuarioRepository.existsByCorreoAndActivoTrue(correo)) {
                    throw new TokenInvalidoException("Usuario inexistente o inactivo");
                }
                var auth = new UsernamePasswordAuthenticationToken(
                        correo, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (TokenInvalidoException ex) {
                SecurityContextHolder.clearContext();
                entryPoint.commence(request, response, new BadCredentialsException(ex.getMessage(), ex));
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIJO)) {
            return null;
        }
        String token = header.substring(PREFIJO.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
