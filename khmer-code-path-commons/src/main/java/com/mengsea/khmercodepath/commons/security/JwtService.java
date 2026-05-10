package com.mengsea.khmercodepath.commons.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final String secret;
    private final Duration expiration;
    private final Duration refreshExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") String expirationSpec,
            @Value("${jwt.refresh-expiration}") String refreshExpirationSpec
    ) {
        this.secret = secret;
        this.expiration = parseDuration(expirationSpec, "jwt.expiration");
        this.refreshExpiration = parseDuration(refreshExpirationSpec, "jwt.refresh-expiration");
    }

    /**
     * Accepts plain seconds (e.g. {@code 3600}) or Spring Boot duration strings
     * (e.g. {@code 1h}, {@code 15m}, {@code 7d}, {@code PT1H}).
     */
    private static Duration parseDuration(String spec, String propertyName) {
        if (spec == null || spec.isBlank()) {
            throw new IllegalArgumentException(propertyName + " must not be blank");
        }
        String s = spec.trim();
        if (s.chars().allMatch(Character::isDigit)) {
            return Duration.ofSeconds(Long.parseLong(s));
        }
        return DurationStyle.detectAndParse(s);
    }

    /** Access token TTL in whole seconds (for API {@code expiresIn}). */
    public long getAccessTokenTtlSeconds() {
        return expiration.toSeconds();
    }

    /** Refresh token TTL in whole seconds (e.g. cookie max-age). */
    public long getRefreshTokenTtlSeconds() {
        return refreshExpiration.toSeconds();
    }

    public String generateToken(UserDetails userDetails) {
        return generateTokenWithExpiry(userDetails, expiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateTokenWithExpiry(userDetails, refreshExpiration);
    }

    private String generateTokenWithExpiry(UserDetails userDetails, Duration ttl) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("roles", userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList());

        Date issuedAt = new Date();
        Date expireAt = new Date(issuedAt.getTime() + ttl.toMillis());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expireAt)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isExpiration(token);
    }

    public boolean isExpiration(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
