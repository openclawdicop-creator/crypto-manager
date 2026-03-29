import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class CheckKeys {
    public static void main(String[] args) throws Exception {
        String privateKeyPem = Files.readString(Path.of("src/main/resources/private_key.pem"))
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");

        String publicKeyPem = Files.readString(Path.of("src/main/resources/public_key.pem"))
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPem);
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPem);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        // Test sign + verify
        byte[] data = "hello world".getBytes();
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(data);
        byte[] signature = signer.sign();

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(data);
        boolean valid = verifier.verify(signature);

        System.out.println("Keys match: " + valid);
        
        // Print public key modulus
        java.security.interfaces.RSAPublicKey rsaPub = (java.security.interfaces.RSAPublicKey) publicKey;
        java.security.interfaces.RSAPrivateKey rsaPriv = (java.security.interfaces.RSAPrivateKey) privateKey;
        System.out.println("Public modulus hash: " + rsaPub.getModulus().hashCode());
        System.out.println("Private modulus hash: " + rsaPriv.getModulus().hashCode());
    }
}
