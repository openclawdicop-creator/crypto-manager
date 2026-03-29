package org.acme.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Usuario;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    public String generateToken(Usuario usuario) {
        Instant now = Instant.now();
        Instant expiry = now.plus(31536000, ChronoUnit.SECONDS); // 1 year

        return Jwt.upn(usuario.username)
                .issuer(issuer)
                .groups("user")
                .issuedAt(now)
                .expiresAt(expiry)
                .sign();
    }
}
