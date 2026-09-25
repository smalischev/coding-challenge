package business;

import domain.CardValue;
import domain.Issue;
import domain.ScrumMaster;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitLabIssueGatewayTest {
    // Prüft, dass das finale Ergebnis als scoped GitLab-Label am Issue gespeichert wird.
    @Test
    void finalResultIsStoredAsGitLabLabel() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Issue issue = new Issue(42);
        RecordingGitLabIssueGateway gitLabIssueGateway = new RecordingGitLabIssueGateway();
        PlanningPokerBusiness business = new PlanningPokerBusiness(gitLabIssueGateway);
        business.createPlanningPoker(scrumMaster, 123L, issue);
        business.releaseActiveIssue(scrumMaster);
        business.reveal(scrumMaster, scrumMaster);

        business.takeToGitlab(scrumMaster, CardValue.EIGHT);

        assertTrue(issue.isApproved());
        assertEquals(123L, gitLabIssueGateway.projectId);
        assertEquals(42L, gitLabIssueGateway.issueIid);
        assertEquals("planning-poker::8", gitLabIssueGateway.label);
    }

    private static class RecordingGitLabIssueGateway implements GitLabIssueGateway {
        private long projectId;
        private long issueIid;
        private String label;

        @Override
        public void addScopedLabel(long projectId, long issueIid, String label) {
            this.projectId = projectId;
            this.issueIid = issueIid;
            this.label = label;
        }
    }
}
