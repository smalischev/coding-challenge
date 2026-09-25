package auth.webservice;

import auth.repository.InMemoryUserRepository;
import auth.JwtCrypto;
import auth.PasswordHasher;
import auth.UserAccount;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    @Inject InMemoryUserRepository userRepository;
    @Inject PasswordHasher passwordHasher;
    @Inject JwtCrypto jwtCrypto;

    @POST
    @Path("/register")
    public Response register(Credentials credentials) {
        validate(credentials);
        userRepository.save(new UserAccount(credentials.username(), passwordHasher.hash(credentials.password())));
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/login")
    public TokenResponse login(Credentials credentials) {
        validate(credentials);
        UserAccount user = userRepository.getByUsername(credentials.username());
        if (!passwordHasher.matches(credentials.password(), user.passwordHash())) {
            throw new IllegalArgumentException("invalid username or password");
        }
        return new TokenResponse(jwtCrypto.createToken(user.username()), "Bearer", 3600);
    }

    private void validate(Credentials credentials) {
        if (credentials == null || credentials.username() == null || credentials.username().isBlank()
                || credentials.password() == null || credentials.password().length() < 12) {
            throw new IllegalArgumentException("username and a password with at least 12 characters are required");
        }
    }

    public record Credentials(String username, String password) { }
    public record TokenResponse(String accessToken, String tokenType, long expiresIn) { }
}
