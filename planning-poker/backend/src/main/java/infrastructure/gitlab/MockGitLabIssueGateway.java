package infrastructure.gitlab;

import domain.Issue;
import jakarta.enterprise.context.ApplicationScoped;
import business.GitLabIssueGateway;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local development replacement for GitLab. It deliberately has no network dependency.
 */
@ApplicationScoped
@MockGitLab
public class MockGitLabIssueGateway implements GitLabIssueGateway {
    private final Map<String, String> labelsByIssue = new ConcurrentHashMap<>();

    public Issue getIssue(long projectId, long issueIid) {
        if (projectId <= 0 || issueIid <= 0) {
            throw new IllegalArgumentException("a GitLab project ID and issue IID are required");
        }
        return switch ((int) issueIid) {
            case 42 -> new Issue(issueIid, "Login überarbeiten", "Die Anmeldung soll verständlicher werden und Fehlermeldungen klar darstellen.");
            case 57 -> new Issue(issueIid, "Benachrichtigungen bündeln", "Mehrere gleichartige Benachrichtigungen sollen zusammengefasst werden.");
            default -> new Issue(issueIid, "Mock-Issue #" + issueIid, "Lokales Mock-Issue für Projekt " + projectId + ".");
        };
    }

    public void addScopedLabel(long projectId, long issueIid, String label) {
        getIssue(projectId, issueIid);
        this.labelsByIssue.put(projectId + ":" + issueIid, label);
    }
}
