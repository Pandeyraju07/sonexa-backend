package com.sonexa.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    private static final long ACCESS_TOKEN_EXPIRATION = 24 * 60 * 60 * 1000L;
    private static final long REFRESH_TOKEN_EXPIRATION = 30L * 24 * 60 * 60 * 1000L;

    private final Environment environment;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    public JwtUtils(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    void validateSecret() {
        boolean prod = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            if (prod) {
                throw new IllegalStateException("JWT_SECRET must be set to a unique value of at least 32 bytes in production");
            }
            log.warn("event=JWT_SECRET_WEAK message=Using development JWT secret. Set JWT_SECRET before production.");
            if (jwtSecret == null || jwtSecret.isBlank()) {
                jwtSecret = "sonexa-dev-only-secret-do-not-use-in-prod";
            }
        }
        if (prod && jwtSecret.toLowerCase().contains("changeme")) {
            throw new IllegalStateException("JWT_SECRET must not use a placeholder value in production");
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String email, Long userId) {
        return generateAccessToken(email, userId, "USER");
    }

    public String generateAccessToken(String email, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "ACCESS");
        claims.put("role", role != null ? role : "USER");
        return createToken(claims, email, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(String email, Long userId) {
        return generateRefreshToken(email, userId, "USER");
    }

    public String generateRefreshToken(String email, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "REFRESH");
        claims.put("role", role != null ? role : "USER");
        return createToken(claims, email, REFRESH_TOKEN_EXPIRATION);
    }

    public String extractRole(String token) {
        try {
            Object role = extractAllClaims(token).get("role");
            return role != null ? String.valueOf(role) : "USER";
        } catch (Exception e) {
            return "USER";
        }
    }

    public String extractTokenType(String token) {
        try {
            Object type = extractAllClaims(token).get("type");
            return type != null ? String.valueOf(type) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Long extractUserId(String token) {
        try {
            Object userId = extractAllClaims(token).get("userId");
            if (userId instanceof Number number) {
                return number.longValue();
            }
            return userId != null ? Long.parseLong(String.valueOf(userId)) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationMs) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, String userEmail) {
        final String email = extractUsername(token);
        return (email.equalsIgnoreCase(userEmail) && !isTokenExpired(token));
    }

    public boolean isAccessToken(String token) {
        String type = extractTokenType(token);
        return type == null || "ACCESS".equalsIgnoreCase(type);
    }

    public boolean isRefreshToken(String token) {
        String type = extractTokenType(token);
        return type == null || "REFRESH".equalsIgnoreCase(type);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
