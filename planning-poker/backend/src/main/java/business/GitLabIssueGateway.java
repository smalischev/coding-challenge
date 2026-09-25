package business;

public interface GitLabIssueGateway {
    void addScopedLabel(long projectId, long issueIid, String label);
}
