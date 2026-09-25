package auth;

import jakarta.enterprise.context.ApplicationScoped;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@ApplicationScoped
public class PasswordHasher {
    private static final int ITERATIONS = 310_000;
    private static final int KEY_LENGTH = 256;
    private final SecureRandom secureRandom = new SecureRandom();

    public String hash(String password) {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        byte[] hash = derive(password.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public boolean matches(String password, String encodedHash) {
        String[] parts = encodedHash.split(":", 2);
        if (parts.length != 2) {
            return false;
        }
        byte[] expected = Base64.getDecoder().decode(parts[1]);
        byte[] actual = derive(password.toCharArray(), Base64.getDecoder().decode(parts[0]));
        return MessageDigest.isEqual(expected, actual);
    }

    private byte[] derive(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("password hashing failed", exception);
        }
    }
}
