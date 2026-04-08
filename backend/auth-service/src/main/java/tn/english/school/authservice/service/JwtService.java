package tn.english.school.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.english.school.authservice.entity.AppUser;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    public String generateToken(AppUser user) {
        long nowMs  = System.currentTimeMillis();
        long expMs  = nowMs + expirationMs;

        return Jwts.builder()
                .header().type("JWT").and()
                .subject(user.getEmail())
                .claims(Map.of(
                        "id",   user.getId(),
                        "name", user.getFirstName() + " " + user.getLastName(),
                        "role", user.getRole().name()
                ))
                .issuedAt(new Date(nowMs))
                .expiration(new Date(expMs))
                .signWith(getKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            return extractClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
