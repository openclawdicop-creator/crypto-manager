import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.Base64;

public class GenerateKeys {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        
        String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
            Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(kp.getPrivate().getEncoded()) +
            "\n-----END PRIVATE KEY-----\n";
            
        String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
            Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(kp.getPublic().getEncoded()) +
            "\n-----END PUBLIC KEY-----\n";
        
        Files.writeString(Path.of("src/main/resources/private_key.pem"), privateKeyPem);
        Files.writeString(Path.of("src/main/resources/public_key.pem"), publicKeyPem);
        
        System.out.println("Keys generated successfully!");
        System.out.println("Private key size: " + kp.getPrivate().getEncoded().length + " bytes");
        System.out.println("Public key size: " + kp.getPublic().getEncoded().length + " bytes");
    }
}
