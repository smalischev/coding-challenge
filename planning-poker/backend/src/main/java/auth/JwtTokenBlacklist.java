package auth;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class JwtTokenBlacklist {
    private final Map<String, Long> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(String tokenId, long expiresAt) {
        revokedTokens.put(tokenId, expiresAt);
    }

    public boolean isRevoked(String tokenId) {
        Long expiresAt = revokedTokens.get(tokenId);
        if (expiresAt == null) return false;
        if (expiresAt <= Instant.now().getEpochSecond()) {
            revokedTokens.remove(tokenId, expiresAt);
            return false;
        }
        return true;
    }
}
