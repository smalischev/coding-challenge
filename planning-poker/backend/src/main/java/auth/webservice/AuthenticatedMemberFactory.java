package auth.webservice;
import domain.*;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthenticatedMemberFactory {
 @Inject SecurityIdentity identity;
 public Member create() {
  String name = identity.getPrincipal().getName();

  if (identity.hasRole("SCRUM_MASTER"))
   return new ScrumMaster(name);

  if (identity.hasRole("DEVELOPER"))
   return new Developer(name);

  throw new NotAllowedException("user has no planning-poker role");
 }
}
