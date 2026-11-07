package com.mall.common.core;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public final class JwtUtil {

    private JwtUtil() {
    }

    public static String create(Long userId, String username, String secret, long expireSeconds) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expireSeconds * 1000))
                .signWith(key(secret))
                .compact();
    }

    public static Claims parse(String token, String secret) {
        return Jwts.parser()
                .verifyWith(key(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
