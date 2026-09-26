package business;

import domain.CardValue;
import domain.Issue;
import domain.ScrumMaster;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

class GitLabIssueGatewayTest {
    // Prüft, dass beim Erstellen einer Session die Details des initialen Issues über GitLab geladen werden.
    @Test
    void initialIssueIsLoadedFromGitLab() {
        RecordingGitLabIssueGateway gitLabIssueGateway = new RecordingGitLabIssueGateway();
        gitLabIssueGateway.loadedIssue = new Issue(42, "Login verbessern", "Die Anmeldung soll verständlicher werden.");
        PlanningPokerBusiness business = new PlanningPokerBusiness(gitLabIssueGateway);

        Issue issue = business.loadIssue(123L, 42L);
        UUID planningPokerId = business.createPlanningPoker(new ScrumMaster("Mara"), 123L, issue);

        Issue activeIssue = business.getActiveIssue(planningPokerId);
        assertEquals(123L, gitLabIssueGateway.requestedProjectId);
        assertEquals(42L, gitLabIssueGateway.requestedIssueIid);
        assertEquals("Login verbessern", activeIssue.getTitle());
        assertEquals("Die Anmeldung soll verständlicher werden.", activeIssue.getDescription());
    }

    // Prüft, dass das finale Ergebnis als scoped GitLab-Label am Issue gespeichert wird.
    @Test
    void finalResultIsStoredAsGitLabLabel() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Issue issue = new Issue(42);
        RecordingGitLabIssueGateway gitLabIssueGateway = new RecordingGitLabIssueGateway();
        PlanningPokerBusiness business = new PlanningPokerBusiness(gitLabIssueGateway);
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, issue);
        business.releaseActiveIssue(planningPokerId, scrumMaster);
        business.reveal(planningPokerId, scrumMaster, scrumMaster);

        business.takeToGitlab(planningPokerId, scrumMaster, CardValue.EIGHT);

        assertTrue(issue.isApproved());
        assertEquals(123L, gitLabIssueGateway.projectId);
        assertEquals(42L, gitLabIssueGateway.issueIid);
        assertEquals("planning-poker::8", gitLabIssueGateway.label);
    }

    private static class RecordingGitLabIssueGateway implements GitLabIssueGateway {
        private long projectId;
        private long issueIid;
        private String label;
        private long requestedProjectId;
        private long requestedIssueIid;
        private Issue loadedIssue;

        @Override
        public Issue getIssue(long projectId, long issueIid) {
            this.requestedProjectId = projectId;
            this.requestedIssueIid = issueIid;
            return loadedIssue;
        }

        @Override
        public void addScopedLabel(long projectId, long issueIid, String label) {
            this.projectId = projectId;
            this.issueIid = issueIid;
            this.label = label;
        }
    }
}
