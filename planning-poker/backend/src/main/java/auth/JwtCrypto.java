package auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@ApplicationScoped
public class JwtCrypto {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ConfigProperty(name = "jwt.secret")
    String secret;

    @PostConstruct
    void validateSecret() {
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 characters");
        }
    }

    public String createToken(String username) {
        try {
            long now = Instant.now().getEpochSecond();
            String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
            String payload = encodeJson(Map.of("sub", username, "iat", now, "exp", now + 3600));
            String unsignedToken = header + "." + payload;
            return unsignedToken + "." + ENCODER.encodeToString(sign(unsignedToken));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT creation failed", exception);
        }
    }

    public String verifyAndGetUsername(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3 || !MessageDigest.isEqual(sign(parts[0] + "." + parts[1]), DECODER.decode(parts[2]))) {
                throw new IllegalArgumentException("invalid JWT");
            }
            Map<String, Object> payload = objectMapper.readValue(DECODER.decode(parts[1]), new TypeReference<>() { });
            if (((Number) payload.get("exp")).longValue() <= Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("JWT has expired");
            }
            return (String) payload.get("sub");
        } catch (Exception exception) {
            throw new IllegalArgumentException("invalid JWT", exception);
        }
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private byte[] sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }
}
