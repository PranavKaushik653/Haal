package com.haal.backend.auth.security;


import com.haal.backend.shared.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMs;
    public JwtService(AppProperties appProperties) {
        this.signingKey= Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(appProperties.getJwt().getSecret()));
        this.expirationMs=appProperties.getJwt().getExpirationMs();
    }
    public String generateAccessToken(UUID userId, String alias, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claims(Map.of(
                        "alias", alias,
                        "role", role
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public Claims validateAndExtractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(validateAndExtractClaims(token).getSubject());
    }

    public boolean isTokenValid(String token) {
        try {
            validateAndExtractClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired");
        } catch (JwtException e) {
            log.debug("JWT invalid: {}", e.getMessage());
        }
        return false;
    }
}
