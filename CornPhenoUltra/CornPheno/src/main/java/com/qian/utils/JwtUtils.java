package com.qian.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys; // 新增：用于密钥生成

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class JwtUtils {

    private static final String signKey = "SVRIRUlNQQ==";
    private static final Long expire = 43_200_000L;

    private static final SecretKey KEY = Keys.hmacShaKeyFor(signKey.getBytes(StandardCharsets.UTF_8));

    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .signWith(KEY)
                .expiration(new Date(System.currentTimeMillis() + expire)) // 0.12.0 中 setExpiration 变为 expiration
                .compact();
    }

    public static Claims parseJWT(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Optional<Long> parseUserId(String token) {
        Claims claims = parseJWT(token);
        return Optional.ofNullable(claims.get("userId", Long.class));
    }
}

