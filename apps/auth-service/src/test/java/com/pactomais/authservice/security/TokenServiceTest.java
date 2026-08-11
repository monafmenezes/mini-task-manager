package com.pactomais.authservice.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.pactomais.authservice.user.User;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class TokenServiceTest {

    private static KeyPair parDeChaves;

    @BeforeAll
    static void gerarParDeChaves() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        parDeChaves = generator.generateKeyPair();
    }

    @Test
    void generateToken_deveEmitirTokenQueAChavePublicaCorrespondenteConsegueValidar() {
        TokenService tokenService = tokenServiceComChave(parDeChaves);
        JwtDecoder decoder =
                NimbusJwtDecoder.withPublicKey((RSAPublicKey) parDeChaves.getPublic()).build();

        User user =
                User.builder()
                        .id(UUID.randomUUID())
                        .nome("Fulano de Tal")
                        .email("fulano@teste.com")
                        .senhaHash("hash")
                        .build();

        String token = tokenService.generateToken(user);
        Jwt decoded = decoder.decode(token);

        assertThat(decoded.getSubject()).isEqualTo(user.getId().toString());
        assertThat(decoded.getClaimAsString("email")).isEqualTo(user.getEmail());
        assertThat(decoded.getClaimAsString("nome")).isEqualTo(user.getNome());
        assertThat(decoded.getClaimAsString("iss")).isEqualTo("auth-service");
        assertThat(decoded.getExpiresAt()).isAfter(decoded.getIssuedAt());
    }

    @Test
    void generateToken_naoDeveSerValidadoPorOutraChavePublica() throws Exception {
        TokenService tokenService = tokenServiceComChave(parDeChaves);

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair parDeOutroServico = generator.generateKeyPair();
        JwtDecoder decoderComChaveErrada =
                NimbusJwtDecoder.withPublicKey((RSAPublicKey) parDeOutroServico.getPublic()).build();

        User user =
                User.builder()
                        .id(UUID.randomUUID())
                        .nome("Fulano de Tal")
                        .email("fulano@teste.com")
                        .senhaHash("hash")
                        .build();

        String token = tokenService.generateToken(user);

        assertThatThrownBy(() -> decoderComChaveErrada.decode(token)).isInstanceOf(JwtException.class);
    }

    private TokenService tokenServiceComChave(KeyPair keyPair) {
        RSAKey rsaKey =
                new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                        .privateKey((RSAPrivateKey) keyPair.getPrivate())
                        .build();
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
        JwtProperties properties = new JwtProperties("n/a", "n/a", "auth-service", 60);
        return new TokenService(encoder, properties);
    }
}
