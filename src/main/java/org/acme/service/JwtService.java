package org.acme.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Usuario;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class JwtService {

    public String generateToken(Usuario usuario) {
        Instant now = Instant.now();
        Instant expiry = now.plus(31536000, ChronoUnit.SECONDS); // 1 year
        
        return Jwt.upn(usuario.username)
                .groups("user")
                .issuedAt(now)
                .expiresAt(expiry)
                .sign();
    }
}
