package com.tlu.hrm.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.tlu.hrm.enums.RoleType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

    // Default safe secret if not provided in properties
    @Value("${jwt.secret:9a4f2c8d3b7a1e5f8g9h0i1j2k3l4m5n6o7p8q9r0s1t2u3v4w5x6y7z8a9b0c1d}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 hours
    private long jwtExpirationInMs;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days
    private long jwtRefreshExpirationInMs;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Authentication authentication) {
        return generateAccessToken((UserDetails) authentication.getPrincipal());
    }

    public String generateAccessToken(UserDetails userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // Get primary role
        String role = userPrincipal.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .findFirst()
                .orElse(RoleType.HR_EMPLOYEE.getName());

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateRefreshToken((UserDetails) authentication.getPrincipal());
    }

    public String generateRefreshToken(UserDetails userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpirationInMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            System.err.println("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.err.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty.");
        }
        return false;
    }
}
