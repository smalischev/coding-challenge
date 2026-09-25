package auth.webservice;

import auth.JwtTokenBlacklist;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class RevokedTokenFilter implements ContainerRequestFilter {
    @Inject JwtTokenBlacklist tokenBlacklist;
    @Inject JsonWebToken jwt;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath().replaceFirst("^/", "");
        if (path.startsWith("auth/login") || path.startsWith("auth/register")) return;
        if (tokenBlacklist.isRevoked(jwt.getTokenID())) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
