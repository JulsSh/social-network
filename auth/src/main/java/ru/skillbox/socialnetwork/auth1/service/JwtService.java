package ru.skillbox.socialnetwork.auth1.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.skillbox.socialnetwork.auth1.entity.User;
import ru.skillbox.socialnetwork.auth1.enums.TokenType;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.access.token}")
    private Long accessTokenExpirationMinutes;

    @Value("${jwt.expiration.refresh.token}")
    private Long refreshTokenExpirationDays;

    private SecretKey secretKey;

    @PostConstruct
    private void init() {
        byte[] bytes = Decoders.BASE64.decode(jwtSecret);
        secretKey = Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccessToken(User user) {
        Instant expiration = Instant.now().plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .claim("token_type", TokenType.ACCESS.name())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant expiration = Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .claim("token_type", TokenType.REFRESH.name())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public boolean isValid(String token, TokenType tokenType) {
        try {
            String result = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("token_type", String.class);
            return result.equals(tokenType.name());

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        String id = Jwts.parser().verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("sub", String.class);
        return UUID.fromString(id);
    }
}
