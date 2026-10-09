package com.proyecto.servicios.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Genera tokens JWT firmados con HS256. El secreto proviene de una variable
 * de entorno (security.jwt.secret). Componente aislado: hoy solo lo usa el
 * endpoint de login; no filtra ni protege otros endpoints todavia.
 */
@Component
public class JwtProvider {

    private final SecretKey key;
    private final long expirationMs;
    private final String issuer;

    public JwtProvider(@Value("${security.jwt.secret}") String secret,
                       @Value("${security.jwt.expiration-ms:3600000}") long expirationMs,
                       @Value("${security.jwt.issuer:onboarding-api}") String issuer) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "security.jwt.secret debe tener al menos 32 caracteres (configura JWT_SECRET en el .env)");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.issuer = issuer;
    }

    /** Resultado de la generacion: token + instantes de emision y expiracion. */
    public record TokenGenerado(String token, Instant emitidoEn, Instant expiraEn, long expiraEnMs) {}

    public TokenGenerado generar(String correo) {
        Instant ahora = Instant.now();
        Instant exp = ahora.plusMillis(expirationMs);
        String token = Jwts.builder()
                .issuer(issuer)
                .subject(correo)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
        return new TokenGenerado(token, ahora, exp, expirationMs);
    }
}
