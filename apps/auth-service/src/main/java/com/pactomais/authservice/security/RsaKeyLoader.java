package com.pactomais.authservice.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class RsaKeyLoader {

    private RsaKeyLoader() {}

    public static RSAPrivateKey loadPrivateKey(String path) {
        try {
            String base64 = stripPem(Files.readString(Path.of(path)));
            byte[] decoded = Base64.getDecoder().decode(base64);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível carregar a chave privada em " + path, e);
        }
    }

    public static RSAPublicKey loadPublicKey(String path) {
        try {
            String base64 = stripPem(Files.readString(Path.of(path)));
            byte[] decoded = Base64.getDecoder().decode(base64);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível carregar a chave pública em " + path, e);
        }
    }

    private static String stripPem(String pem) {
        return pem.replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
    }
}
