package business;

import domain.Issue;

public interface GitLabIssueGateway {
    default Issue getIssue(long projectId, long issueIid) {
        throw new UnsupportedOperationException("retrieving GitLab issues is not supported");
    }

    void addScopedLabel(long projectId, long issueIid, String label);
}
