package auth;
import domain.Role;
public record UserAccount(String username, String passwordHash, Role role) {
}
