package com.mamaruo.hospitalinquiry.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;

@Service
public class JwtService {
    public String generateToken(String mobile) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
        .claims()
        .add(claims)
        .subject(mobile)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)).and()
        .signWith(getKey())
        .compact();
    }

    private Key getKey() {
        byte[] keyBytes = "aWoionFNOAINwonodaohtyioifbbeutwqzwdddWfoqwrznvWSaWapspnf".getBytes();
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }
}
