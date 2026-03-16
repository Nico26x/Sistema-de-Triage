package com.triage.backend.security;

import com.triage.backend.domain.enums.RolNombre;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expSeconds;

    public JwtUtil(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expSeconds:3600}") long expSeconds
    ) {
        // HS256 requiere al menos 32 bytes
        if (secret == null || secret.trim().length() < 32) {
            throw new IllegalArgumentException("security.jwt.secret debe tener al menos 32 caracteres.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expSeconds = expSeconds;
    }

    public String generarToken(Long userId, String email, RolNombre rol) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expSeconds);

        return Jwts.builder()
                .subject(email)
                .claim("uid", userId)
                .claim("rol", rol.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            getAllClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String extraerEmail(String token) {
        return getAllClaims(token).getSubject();
    }

    public Long extraerUserId(String token) {
        Object uid = getAllClaims(token).get("uid");
        if (uid instanceof Integer i) return i.longValue();
        if (uid instanceof Long l) return l;
        if (uid instanceof String s) return Long.parseLong(s);
        return null;
    }

    public String extraerRol(String token) {
        Object rol = getAllClaims(token).get("rol");
        return rol == null ? null : rol.toString();
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}