package org.acme;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtKeyPairConsistencyTest {

    @Test
    void publicKeyMustMatchPrivateKey() throws Exception {
        RSAPrivateCrtKey privateKey = readPrivateKey();
        RSAPublicKey configuredPublicKey = readPublicKey();

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKey derivedPublicKey = (RSAPublicKey) keyFactory.generatePublic(
                new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent())
        );

        assertEquals(derivedPublicKey.getModulus(), configuredPublicKey.getModulus());
        assertEquals(derivedPublicKey.getPublicExponent(), configuredPublicKey.getPublicExponent());
    }

    private RSAPrivateCrtKey readPrivateKey() throws IOException, GeneralSecurityException {
        byte[] keyBytes = decodePem(readResource("private_key.pem"), "PRIVATE KEY");
        return (RSAPrivateCrtKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private RSAPublicKey readPublicKey() throws IOException, GeneralSecurityException {
        byte[] keyBytes = decodePem(readResource("public_key.pem"), "PUBLIC KEY");
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    private String readResource(String resourceName) throws IOException {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new IOException("Resource not found: " + resourceName);
            }
            return new String(input.readAllBytes(), StandardCharsets.US_ASCII);
        }
    }

    private byte[] decodePem(String pem, String type) {
        String header = "-----BEGIN " + type + "-----";
        String footer = "-----END " + type + "-----";
        String base64 = pem.replace(header, "")
                .replace(footer, "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
