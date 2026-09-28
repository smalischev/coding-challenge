package domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

public class PlanningPoker {
    private final UUID id;
    private final Session session;
    private final long gitlabProjektID;
    private final List<Issue> issues;
    private Issue activeIssue;
    private EstimationRound currentRound;
    private final List<Developer> developers;
    private final Map<Developer, Instant> developerJoinedAt;
    private final ScrumMaster owner;
    private final List<CompletedRound> completedRounds;

    public PlanningPoker(ScrumMaster owner, long gitlabProjektID, Issue issue) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.developers = new ArrayList<>();
        this.developerJoinedAt = new LinkedHashMap<>();
        this.completedRounds = new ArrayList<>();
        this.issues = new ArrayList<>();
        this.session = new Session();
        this.gitlabProjektID = gitlabProjektID;
        this.activeIssue = issue;
        this.selectIssue(owner, this.activeIssue);
    }

    public UUID getId() {
        return id;
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

        completedRounds.add(createCompletedRound());
        this.currentRound = new EstimationRound(activeIssue);
    }

    public void join(Member member, Developer developer) {
        if(this.currentRound == null)
            throw new RuntimeException("no estimation round created");

        if(member.getRole() != Role.DEVELOPER){
            throw new NotAllowedException("not allowed, only developer");
        }
        if (!member.equals(developer)) {
            throw new NotAllowedException("not allowed, a developer can only join for themselves");
        }

        if (developers.contains(developer)) {
            return;
        }

        if(this.currentRound.isReleased())
            throw new RuntimeException("cannot join member after released estimation round");

        developers.add(developer);
        developerJoinedAt.put(developer, Instant.now());
    }

    public void estimate(Member member, Developer developer, CardValue cardValue) {
        if(member.getRole() != Role.DEVELOPER){
            throw new NotAllowedException("not allowed, only developer");
        }
        if (!member.equals(developer)) {
            throw new NotAllowedException("not allowed, a developer can only estimate for themselves");
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
        if (!scrumMaster.equals(owner)) {
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

    public Issue finalizeResult(ScrumMaster scrumMaster, CardValue value) {
        if (!scrumMaster.equals(this.owner)) {
            throw new NotAllowedException("not allowed, only the session owner can finalize the result");
        }

        if (activeIssue == null) {
            throw new IllegalStateException("no issue selected");
        }

        if (currentRound == null || !currentRound.isRevealed()) {
            throw new IllegalStateException("estimation round has not been revealed yet");
        }

        activeIssue.approve(value);
        return activeIssue;
    }

    public long getGitlabProjectId() {
        return gitlabProjektID;
    }

    public Issue getActiveIssue() {
        if (activeIssue == null) {
            throw new IllegalStateException("no active issue selected");
        }
        return activeIssue;
    }

    public boolean isSessionOwner(ScrumMaster scrumMaster) {
        return owner.equals(scrumMaster);
    }

    public EstimationProgress getEstimationProgress() {
        if (currentRound == null) {
            throw new IllegalStateException("no active estimation round");
        }

        Set<Developer> estimatedDevelopers = currentRound.getEstimatedDevelopers();
        Set<Developer> pendingDevelopers = new LinkedHashSet<>(developers);
        pendingDevelopers.removeAll(estimatedDevelopers);

        return new EstimationProgress(
                owner.getName(),
                estimatedDevelopers,
                pendingDevelopers,
                developerJoinedAt,
                currentRound.isReleased(),
                currentRound.isRevealed()
        );
    }

    public Map<CardValue, Long> groupEstimates() {
        if (currentRound == null) {
            throw new IllegalStateException("no active estimation round");
        }

        return currentRound.groupEstimates();
    }

    public OptionalInt calculateAverage() {
        if (currentRound == null) {
            throw new IllegalStateException("no active estimation round");
        }

        return currentRound.calculateAverage();
    }

    public OptionalInt findMostFrequentValue() {
        if (currentRound == null) {
            throw new IllegalStateException("no active estimation round");
        }

        return currentRound.findMostFrequentValue();
    }

    public List<CompletedRound> getCompletedRounds() {
        return List.copyOf(completedRounds);
    }

    private CompletedRound createCompletedRound() {
        Map<String, CardValue> estimates = currentRound.getEstimateValues().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        entry -> entry.getKey().getName(),
                        Map.Entry::getValue,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        return new CompletedRound(
                completedRounds.size() + 1,
                activeIssue.getGitlabIssueIid(),
                activeIssue.getTitle(),
                Instant.now(),
                estimates
        );
    }

    private void ensureSessionOwner(Member member) {
        if (!member.equals(owner)) {
            throw new NotAllowedException("not allowed, only the session owner can perform this action");
        }
    }
}
