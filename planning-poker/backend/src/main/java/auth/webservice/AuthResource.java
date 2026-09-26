package auth.webservice;

import auth.repository.InMemoryUserRepository;
import auth.PasswordHasher;
import auth.UserAccount;
import domain.Role;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.RolesAllowed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.jwt.JsonWebToken;
import auth.JwtTokenBlacklist;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Authentifizierung", description = "Registrierung, Login und Logout von Planning-Poker-Benutzern")
public class AuthResource {
    @Inject InMemoryUserRepository userRepository;
    @Inject PasswordHasher passwordHasher;
    @Inject JwtTokenBlacklist tokenBlacklist;
    @Inject JsonWebToken jwt;

    @POST
    @Path("/register")
    @Operation(summary = "Benutzer registrieren", description = "Registriert einen Benutzer dauerhaft als Developer oder Scrum Master.")
    @APIResponse(responseCode = "201", description = "Benutzer wurde registriert")
    public Response register(Credentials credentials) {
        validate(credentials, true);
        userRepository.save(new UserAccount(credentials.username(), passwordHasher.hash(credentials.password()), credentials.role()));
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/login")
    @Operation(summary = "Anmelden", description = "Prüft Benutzername und Passwort und liefert ein signiertes Bearer-JWT mit der Benutzerrolle zurück.")
    @APIResponse(responseCode = "200", description = "Login war erfolgreich; Antwort enthält ein Access Token")
    public TokenResponse login(Credentials credentials) {
        validate(credentials, false);
        UserAccount user = userRepository.getByUsername(credentials.username());
        if (!passwordHasher.matches(credentials.password(), user.passwordHash())) {
            throw new IllegalArgumentException("invalid username or password");
        }
        return new TokenResponse(Jwt.upn(user.username()).groups(user.role().name()).sign(), "Bearer", 3600);
    }

    @POST
    @Path("/logout")
    @RolesAllowed({"SCRUM_MASTER", "DEVELOPER"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Abmelden", description = "Sperrt das aktuelle JWT bis zu seinem Ablaufzeitpunkt.")
    @APIResponse(responseCode = "204", description = "Token wurde gesperrt")
    public Response logout() {
        tokenBlacklist.revoke(jwt.getTokenID(), jwt.getExpirationTime());
        return Response.noContent().build();
    }

    private void validate(Credentials credentials, boolean roleRequired) {
        if (credentials == null || credentials.username() == null || credentials.username().isBlank()
                || credentials.password() == null || credentials.password().length() < 12 || (roleRequired && credentials.role() == null)) {
            throw new IllegalArgumentException("username and a password with at least 12 characters are required");
        }
    }

    public record Credentials(String username, String password, Role role) { }
    public record TokenResponse(String accessToken, String tokenType, long expiresIn) { }
}
