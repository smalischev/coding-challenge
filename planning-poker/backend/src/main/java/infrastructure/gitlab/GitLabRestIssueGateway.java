package infrastructure.gitlab;

import business.GitLabIssueGateway;
import domain.Issue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class GitLabRestIssueGateway implements GitLabIssueGateway {
    @Inject
    @RestClient
    GitLabRestClient gitLabRestClient;

    @Inject
    @ConfigProperty(name = "gitlab.token")
    String accessToken;

    @Override
    public Issue getIssue(long projectId, long issueIid) {
        if (projectId <= 0 || issueIid <= 0) {
            throw new IllegalArgumentException("a GitLab project ID and issue IID are required");
        }

        GitLabRestClient.GitLabIssueResponse response = gitLabRestClient.getIssue(accessToken, projectId, issueIid);
        return new Issue(response.iid(), response.title(), response.description());
    }

    @Override
    public void addScopedLabel(long projectId, long issueIid, String label) {
        if (issueIid <= 0) {
            throw new IllegalArgumentException("a GitLab issue IID is required");
        }

        gitLabRestClient.addLabel(accessToken, projectId, issueIid, label);
    }
}
