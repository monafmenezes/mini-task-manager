package com.pactomais.authservice.security;

import com.pactomais.authservice.user.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public String generateToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(jwtProperties.issuer())
                        .issuedAt(now)
                        .expiresAt(now.plus(jwtProperties.expirationMinutes(), ChronoUnit.MINUTES))
                        .subject(user.getId().toString())
                        .claim("email", user.getEmail())
                        .claim("nome", user.getNome())
                        .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public long expirationSeconds() {
        return jwtProperties.expirationMinutes() * 60;
    }
}
