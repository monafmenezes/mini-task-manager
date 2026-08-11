package com.pactomais.authservice.auth;

public record LoginResponse(String token, String tokenType, long expiresInSeconds) {}
