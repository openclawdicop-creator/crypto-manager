import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class GenerateKeys {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        
        String privKey = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(kp.getPrivate().getEncoded()) +
                "\n-----END PRIVATE KEY-----\n";
        String pubKey = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(kp.getPublic().getEncoded()) +
                "\n-----END PUBLIC KEY-----\n";
                
        try (FileOutputStream fos = new FileOutputStream("src/main/resources/private_key.pem")) {
            fos.write(privKey.getBytes());
        }
        try (FileOutputStream fos = new FileOutputStream("src/main/resources/public_key.pem")) {
            fos.write(pubKey.getBytes());
        }
        System.out.println("Keys generated successfully in src/main/resources!");
    }
}
