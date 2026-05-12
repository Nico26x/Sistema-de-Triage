package com.triage.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expSeconds}")
    private long expirationSeconds;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generar un token JWT con los datos del usuario.
     */
    public String generarToken(Long usuarioId, String email, String rol) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + (expirationSeconds * 1000));

        return Jwts.builder()
                .subject(email)
                .claim("usuarioId", usuarioId)
                .claim("email", email)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getKey())
                .compact();
    }

    /**
     * Validar que el token sea válido.
     */
    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token expirado
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            // Token inválido
            return false;
        }
    }

    /**
     * Extraer el email (subject) del token.
     */
    public String extraerEmail(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extraer el ID del usuario del token.
     */
    public Long extraerUsuarioId(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("usuarioId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extraer el rol del token.
     */
    public String extraerRol(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("rol", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verificar si el token está expirado.
     */
    public boolean esTokenExpirado(String token) {
        try {
            Date expiracion = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return expiracion != null && expiracion.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
