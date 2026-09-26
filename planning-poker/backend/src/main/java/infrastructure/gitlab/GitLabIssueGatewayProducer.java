package infrastructure.gitlab;

import business.GitLabIssueGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class GitLabIssueGatewayProducer {
    @Inject @MockGitLab
    GitLabIssueGateway mockGateway;

    @Inject @RestGitLab
    GitLabIssueGateway restGateway;

    @Inject
    @ConfigProperty(name = "quarkus.profile", defaultValue = "prod")
    String profile;

    @Produces
    @ApplicationScoped
    GitLabIssueGateway gateway() {
        return "dev".equals(profile) ? mockGateway : restGateway;
    }
}
