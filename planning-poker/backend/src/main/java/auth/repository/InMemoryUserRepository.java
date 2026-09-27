package auth.repository;

import auth.UserAccount;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryUserRepository {
    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();

    public boolean save(UserAccount user) {
        return users.putIfAbsent(user.username(), user) == null;
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    public void delete(String username) {
        users.remove(username);
    }

    public UserAccount getByUsername(String username) {
        UserAccount user = users.get(username);
        if (user == null) {
            throw new IllegalArgumentException("invalid username or password");
        }
        return user;
    }
}
