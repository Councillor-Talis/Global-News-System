package com.gns.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${gns.jwt.secret}")
    private String secret;

    @Value("${gns.jwt.expire}")
    private Long expire;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 生成 token */
    public String generateToken(Long userId, String username, Integer role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expire * 1000))
                .signWith(getKey())
                .compact();
    }

    /** 解析 token */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token)    { return Long.parseLong(parseToken(token).getSubject()); }
    public String getUsername(String token){ return parseToken(token).get("username", String.class); }
    public Integer getRole(String token)   { return parseToken(token).get("role", Integer.class); }

    public boolean isValid(String token) {
        try { parseToken(token); return true; }
        catch (Exception e) { return false; }
    }
}