package auth.webservice;

import auth.JwtTokenBlacklist;
import auth.repository.InMemoryUserRepository;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class RevokedTokenFilter implements ContainerRequestFilter {
    @Inject JwtTokenBlacklist tokenBlacklist;
    @Inject InMemoryUserRepository userRepository;
    @Inject JsonWebToken jwt;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath().replaceFirst("^/", "");
        if (path.startsWith("auth/login") || path.startsWith("auth/register")) return;
        if (requestContext.getHeaderString(HttpHeaders.AUTHORIZATION) == null) return;

        String username = jwt.getSubject();
        if (username == null || !userRepository.exists(username)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        if (tokenBlacklist.isRevoked(jwt.getTokenID())) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
