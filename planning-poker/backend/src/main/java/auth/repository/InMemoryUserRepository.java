package auth.repository;

import auth.UserAccount;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryUserRepository {
    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();

    public void save(UserAccount user) {
        if (users.putIfAbsent(user.username(), user) != null) {
            throw new IllegalArgumentException("username is already registered");
        }
    }

    public UserAccount getByUsername(String username) {
        UserAccount user = users.get(username);
        if (user == null) {
            throw new IllegalArgumentException("invalid username or password");
        }
        return user;
    }
}
