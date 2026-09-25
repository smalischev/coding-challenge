package domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlanningPoker {
    private final Session session;
    private final long gitlabProjektID;
    private final List<Issue> issues;
    private Issue activeIssue;
    private EstimationRound currentRound;
    private final List<Developer> developers;
    private final ScrumMaster owner;

    public PlanningPoker(ScrumMaster owner, long gitlabProjektID, Issue issue) {
        this.owner = owner;
        this.developers = new ArrayList<>();
        this.issues = new ArrayList<>();
        this.session = new Session();
        this.gitlabProjektID = gitlabProjektID;
        this.activeIssue = issue;
        this.selectIssue(owner, this.activeIssue);
    }

    public void selectIssue(Member member, Issue issue) {
        ensureSessionOwner(member);

        if (currentRound != null && currentRound.isRevealed()) {
            throw new IllegalStateException("issue cannot be changed after the round was revealed");
        }

        if (!issues.contains(issue)) {
            issues.add(issue);
        }

        this.activeIssue = issue;
        this.currentRound = new EstimationRound(issue);
    }

    public void releaseActiveIssue(Member member) {
        ensureSessionOwner(member);

        if (currentRound == null || activeIssue == null) {
            throw new IllegalStateException("no issue selected");
        }

        currentRound.release();
    }

    public void startNewRound(Member member) {
        ensureSessionOwner(member);

        if (activeIssue == null) {
            throw new IllegalStateException("no issue selected");
        }
        if (currentRound == null || !currentRound.isRevealed()) {
            throw new IllegalStateException("current estimation round has not been revealed yet");
        }

        this.currentRound = new EstimationRound(activeIssue);
    }

    public void join(Member member, Developer developer) {
        if(this.currentRound == null)
            throw new RuntimeException("no estimation round created");

        if(member.getRole() != Role.DEVELOPER){
            throw new NotAllowedException("not allowed, only developer");
        }

        if(this.currentRound.isReleased())
            throw new RuntimeException("cannot join member after released estimation round");

        if (developers.contains(developer)) {
            throw new RuntimeException("member already joined the estimation round");
        }

        developers.add(developer);
    }

    public void estimate(Member member, Developer developer, CardValue cardValue) {
        if(member.getRole() != Role.DEVELOPER){
            throw new NotAllowedException("not allowed, only developer");
        }

        if(!this.currentRound.isReleased()){
            throw new RuntimeException("cannot estimate because estimate round has not been released yet");
        }
        if (!developers.contains(developer)) {
            throw new RuntimeException("unknown developer. please contact scrum master to join you into this planning poker");
        }
        if (currentRound == null || activeIssue == null) {
            throw new IllegalStateException("no active issue selected");
        }

        currentRound.estimate(developer, cardValue);
    }

    public boolean allDevelopersEstimated() {
        return currentRound != null && currentRound.allDevelopersEstimated(developers);
    }

    // Der Scrum Master löst die Schätzrunde jederzeit auf, auch wenn noch
    // nicht alle Teilnehmer geschätzt haben. Danach werden die Einzelwerte,
    // die Gruppenauswertung, der Durchschnitt und der häufigste Wert sichtbar.
    public void reveal(Member member, ScrumMaster scrumMaster) {
        ensureSessionOwner(member);
        if (scrumMaster != owner) {
            throw new NotAllowedException("not allowed, only the session owner can reveal estimates");
        }

        if (currentRound == null) {
            throw new IllegalStateException("no active estimation round");
        }

        currentRound.reveal(scrumMaster, this.owner);
    }

    public Map<Developer, CardValue> getEstimateValues() {
        if (currentRound == null) {
            throw new IllegalStateException("no active estimation round");
        }

        return currentRound.getEstimateValues();
    }

    public void finalizeResult(ScrumMaster scrumMaster, CardValue value) {
        if (scrumMaster != this.owner) {
            throw new NotAllowedException("not allowed, only the session owner can finalize the result");
        }

        if (activeIssue == null) {
            throw new IllegalStateException("no issue selected");
        }

        if (currentRound == null || !currentRound.isRevealed()) {
            throw new IllegalStateException("estimation round has not been revealed yet");
        }

        activeIssue.approve(value);
    }

    public EstimationProgress getEstimationProgress() {
        if (currentRound == null) {
            throw new IllegalStateException("no active estimation round");
        }

        Set<Developer> estimatedDevelopers = currentRound.getEstimatedDevelopers();
        Set<Developer> pendingDevelopers = new LinkedHashSet<>(developers);
        pendingDevelopers.removeAll(estimatedDevelopers);

        return new EstimationProgress(estimatedDevelopers, pendingDevelopers);
    }

    public Map<CardValue, Long> groupEstimates() {
        if (currentRound == null) {
            throw new IllegalStateException("no active estimation round");
        }

        return currentRound.groupEstimates();
    }

    public int calculateAverage() {
        if (currentRound == null) {
            throw new IllegalStateException("no active estimation round");
        }

        return currentRound.calculateAverage();
    }

    public int findMostFrequentValue() {
        if (currentRound == null) {
            throw new IllegalStateException("no active estimation round");
        }

        return currentRound.findMostFrequentValue();
    }

    private void ensureSessionOwner(Member member) {
        if (member != owner) {
            throw new NotAllowedException("not allowed, only the session owner can perform this action");
        }
    }
}
