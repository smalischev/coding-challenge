package business;

import domain.*;
import repository.InMemoryPlanningPokerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class PlanningPokerBusiness {
    private static final String PLANNING_POKER_LABEL_PREFIX = "planning-poker::";

    private final GitLabIssueGateway gitLabIssueGateway;
    private final PlanningPokerRepository planningPokerRepository;
    private final Event<AllDevelopersEstimated> allDevelopersEstimatedEvents;
    private final Set<UUID> notifiedCompletedRounds = ConcurrentHashMap.newKeySet();

    @Inject
    public PlanningPokerBusiness(
            GitLabIssueGateway gitLabIssueGateway,
            PlanningPokerRepository planningPokerRepository,
            Event<AllDevelopersEstimated> allDevelopersEstimatedEvents
    ) {
        this.gitLabIssueGateway = gitLabIssueGateway;
        this.planningPokerRepository = planningPokerRepository;
        this.allDevelopersEstimatedEvents = allDevelopersEstimatedEvents;
    }

    public PlanningPokerBusiness(GitLabIssueGateway gitLabIssueGateway, PlanningPokerRepository planningPokerRepository) {
        this(gitLabIssueGateway, planningPokerRepository, null);
    }

    // Kompatibilitätskonstruktor für reine Java-Tests ohne CDI.
    public PlanningPokerBusiness(GitLabIssueGateway gitLabIssueGateway) {
        this(gitLabIssueGateway, new InMemoryPlanningPokerRepository(), null);
    }

    public UUID createPlanningPoker(ScrumMaster scrumMaster, long gitlabProjectID, Issue issue) {
        PlanningPoker planningPoker = new PlanningPoker(scrumMaster, gitlabProjectID, issue);
        planningPokerRepository.save(planningPoker);
        return planningPoker.getId();
    }

    public void join(UUID planningPokerId, Member member, Developer developer) {
        PlanningPoker planningPoker = getPlanningPoker(planningPokerId);
        planningPoker.join(member, developer);
        save(planningPoker);
    }

    public void selectIssue(UUID planningPokerId, Member member, Issue issue) {
        PlanningPoker planningPoker = getPlanningPoker(planningPokerId);
        planningPoker.selectIssue(member, issue);
        save(planningPoker);
        notifiedCompletedRounds.remove(planningPokerId);
    }

    public void releaseActiveIssue(UUID planningPokerId, Member member) {
        PlanningPoker planningPoker = getPlanningPoker(planningPokerId);
        planningPoker.releaseActiveIssue(member);
        save(planningPoker);
    }

    public void estimate(UUID planningPokerId, Member member, Developer developer, CardValue cardValue) {
        PlanningPoker planningPoker = getPlanningPoker(planningPokerId);
        planningPoker.estimate(member, developer, cardValue);
        save(planningPoker);
        if (planningPoker.allDevelopersEstimated() && notifiedCompletedRounds.add(planningPokerId)
                && allDevelopersEstimatedEvents != null) {
            allDevelopersEstimatedEvents.fire(new AllDevelopersEstimated(planningPokerId));
        }
    }

    public void reveal(UUID planningPokerId, Member member, ScrumMaster scrumMaster) {
        PlanningPoker planningPoker = getPlanningPoker(planningPokerId);
        planningPoker.reveal(member, scrumMaster);
        save(planningPoker);
    }

    public void startNewRound(UUID planningPokerId, Member member) {
        PlanningPoker planningPoker = getPlanningPoker(planningPokerId);
        planningPoker.startNewRound(member);
        save(planningPoker);
        notifiedCompletedRounds.remove(planningPokerId);
    }

    public void takeToGitlab(UUID planningPokerId, ScrumMaster scrumMaster, CardValue value) {
        PlanningPoker planningPoker = getPlanningPoker(planningPokerId);
        Issue issue = planningPoker.finalizeResult(scrumMaster, value);
        String label = PLANNING_POKER_LABEL_PREFIX + value.getLabelValue();
        gitLabIssueGateway.addScopedLabel(planningPoker.getGitlabProjectId(), issue.getGitlabIssueIid(), label);
        save(planningPoker);
    }

    public EstimationProgress getEstimationProgress(UUID planningPokerId) {
        return getPlanningPoker(planningPokerId).getEstimationProgress();
    }

    public boolean allDevelopersEstimated(UUID planningPokerId) {
        return getPlanningPoker(planningPokerId).allDevelopersEstimated();
    }

    public boolean isSessionOwner(UUID planningPokerId, ScrumMaster scrumMaster) {
        return getPlanningPoker(planningPokerId).isSessionOwner(scrumMaster);
    }

    public Map<Developer, CardValue> getEstimateValues(UUID planningPokerId) {
        return getPlanningPoker(planningPokerId).getEstimateValues();
    }

    public Map<CardValue, Long> groupEstimates(UUID planningPokerId) {
        return getPlanningPoker(planningPokerId).groupEstimates();
    }

    public OptionalInt calculateAverage(UUID planningPokerId) {
        return getPlanningPoker(planningPokerId).calculateAverage();
    }

    public OptionalInt findMostFrequentValue(UUID planningPokerId) {
        return getPlanningPoker(planningPokerId).findMostFrequentValue();
    }

    public void finalizeResult(UUID planningPokerId, ScrumMaster scrumMaster, CardValue value) {
        PlanningPoker planningPoker = getPlanningPoker(planningPokerId);
        planningPoker.finalizeResult(scrumMaster, value);
        save(planningPoker);
    }

    private PlanningPoker getPlanningPoker(UUID planningPokerId) {
        if (planningPokerId == null) {
            throw new IllegalStateException("no planning poker session created");
        }
        return planningPokerRepository.getById(planningPokerId);
    }

    private void save(PlanningPoker planningPoker) {
        planningPokerRepository.save(planningPoker);
    }
}
