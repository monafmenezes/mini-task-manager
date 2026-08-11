package com.pactomais.authservice.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String privateKeyPath, String publicKeyPath, String issuer, long expirationMinutes) {}
