package business;

import domain.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.OptionalInt;

@ApplicationScoped
public class PlanningPokerBusiness {
    private static final String PLANNING_POKER_LABEL_PREFIX = "planning-poker::";

    private final GitLabIssueGateway gitLabIssueGateway;
    private PlanningPoker planningPoker;

    @Inject
    public PlanningPokerBusiness(GitLabIssueGateway gitLabIssueGateway) {
        this.gitLabIssueGateway = gitLabIssueGateway;
    }

    //Scrum master ertellt eine neue planning-poker-sitzung
    public void createPlanningPoker(ScrumMaster scrumMaster, long gitlabProjectID, Issue issue){
        this.planningPoker = new PlanningPoker(scrumMaster, gitlabProjectID, issue);
    }

    //weitere Teilnehmer können der Sitzung beitreten.
    public void join(Member member, Developer developer) {
        planningPoker.join(member, developer);
    }

    //scrum master wählt issue aus, das als nächstes geschätzt werden soll
    //scrum master kann aktive issue wechseln, solange die runde nicht abgeschlossen ist.
    public void selectIssue(Member member, Issue issue) {
        planningPoker.selectIssue(member, issue);
    }

    //erst nach freigabe des scrum master, können schätzungen abgegeben werden
    public void releaseActiveIssue(Member member) {
        planningPoker.releaseActiveIssue(member);
    }

    //entwickler geben schätzung ab.
    public void estimate(Member member, Developer developer, CardValue cardValue) {
        planningPoker.estimate(member, developer, cardValue);
    }

    //scrum master löst jederzeit die runde auf
    public void reveal(Member member, ScrumMaster scrumMaster) {
        planningPoker.reveal(member, scrumMaster);
    }

    //nach auflösung, (A) kann neue runde starten für das gleiche Issue
    //alle bisherigen Karten werden zurückgesetzt
    public void startNewRound(Member member) {
        planningPoker.startNewRound(member);
    }

    // (B) wert in das Gitlab-Issue übernehmen
    public void takeToGitlab(ScrumMaster scrumMaster, CardValue value) {
        Issue issue = planningPoker.finalizeResult(scrumMaster, value);
        String label = PLANNING_POKER_LABEL_PREFIX + value.getLabelValue();

        gitLabIssueGateway.addScopedLabel(
                planningPoker.getGitlabProjectId(),
                issue.getGitlabIssueIid(),
                label
        );
    }

    public EstimationProgress getEstimationProgress(){
        return planningPoker.getEstimationProgress();
    }

    public boolean allDevelopersEstimated() {
        return planningPoker.allDevelopersEstimated();
    }

    public Map<Developer, CardValue> getEstimateValues() {
        return planningPoker.getEstimateValues();
    }

    public Map<CardValue, Long> groupEstimates() {
        return planningPoker.groupEstimates();
    }

    public OptionalInt calculateAverage() {
        return planningPoker.calculateAverage();
    }

    public OptionalInt findMostFrequentValue() {
        return planningPoker.findMostFrequentValue();
    }

    public void finalizeResult(ScrumMaster scrumMaster, CardValue value) {
        planningPoker.finalizeResult(scrumMaster, value);
    }
}
