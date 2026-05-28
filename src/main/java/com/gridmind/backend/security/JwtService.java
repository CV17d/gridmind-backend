package com.gridmind.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority; // Importación necesaria
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collection; // Importación necesaria
import java.util.Date;
import java.util.HashMap; // Importación necesaria
import java.util.List;
import java.util.Map; // Importación necesaria
import java.util.function.Function;
import java.util.stream.Collectors; // Importación necesaria

@Service
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Método original para generar token sin roles (podría ser útil para ciertos casos)
    public String generateToken(String email) {
        return generateToken(email, new HashMap<>());
    }

    // Nuevo método para generar token con roles
    public String generateToken(String email, Collection<? extends GrantedAuthority> authorities) {
        Map<String, Object> claims = new HashMap<>();
        if (authorities != null && !authorities.isEmpty()) {
            claims.put("roles", authorities.stream()
                                            .map(GrantedAuthority::getAuthority)
                                            .collect(Collectors.toList()));
        }

        return Jwts.builder()
                .setClaims(claims) // Añade los claims, incluyendo los roles
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Nuevo método para extraer roles
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");
    }

    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
