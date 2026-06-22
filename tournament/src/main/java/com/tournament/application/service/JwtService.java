package com.tournament.application.service;


import com.tournament.domain.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {


    private final SecretKey signingKey;
    private final long      accessTokenExpMs;
    private final long      refreshTokenExpMs;

    public JwtService(
            @Value("${app.jwt.secret}")                        String secret,
            @Value("${app.jwt.access-token-expiration-ms}")   long accessTokenExpMs,
            @Value("${app.jwt.refresh-token-expiration-ms}")  long refreshTokenExpMs) {

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey        = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpMs  = accessTokenExpMs;
        this.refreshTokenExpMs = refreshTokenExpMs;
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("uid",  user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpMs))
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("uid", Long.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isAccessTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException e) {
            return false;
        }
    }

    public long getAccessTokenExpMs() { return accessTokenExpMs; }

    public long getRefreshTokenExpMs() { return refreshTokenExpMs; }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
