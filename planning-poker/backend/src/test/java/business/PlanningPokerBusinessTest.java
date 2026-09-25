package business;

import domain.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;


class PlanningPokerBusinessTest {
    @Test
    void join() {
        ScrumMaster scrumMaster = new ScrumMaster("test scrum master");
        Issue issue = new Issue();
        PlanningPoker planningPoker = new PlanningPoker(scrumMaster, 123L, issue);

        Developer developer = new Developer("test developer");

        assertThrows(RuntimeException.class, () ->
                planningPoker.estimate(developer, developer, CardValue.ONE)
        );

        planningPoker.join(developer, developer);

        planningPoker.selectIssue(scrumMaster, new Issue());
        planningPoker.releaseActiveIssue(scrumMaster);
        planningPoker.estimate(developer, developer, CardValue.ONE);
    }

    @Test
    void happyPath() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer firstDeveloper = new Developer("Alex");
        Developer secondDeveloper = new Developer("Kim");
        Issue issue = new Issue();
        PlanningPokerBusiness business = new PlanningPokerBusiness();

        business.createPlanningPoker(scrumMaster, 123L, issue);
        business.join(firstDeveloper, firstDeveloper);
        business.join(secondDeveloper, secondDeveloper);

        business.releaseActiveIssue(scrumMaster);
        business.estimate(firstDeveloper, firstDeveloper, CardValue.FIVE);

        Map<Developer, CardValue> estimationProgress = business.getDevelopersEstimated();
        assertEquals(CardValue.FIVE, estimationProgress.get(firstDeveloper));
        assertNull(estimationProgress.get(secondDeveloper));
        assertFalse(business.allDevelopersEstimated());

        business.estimate(secondDeveloper, secondDeveloper, CardValue.EIGHT);
        assertTrue(business.allDevelopersEstimated());

        business.reveal(scrumMaster, scrumMaster);
        assertEquals(Map.of(firstDeveloper, CardValue.FIVE, secondDeveloper, CardValue.EIGHT),
                business.getEstimateValues());
        assertEquals(6, business.calculateAverage());
        assertEquals(1L, business.groupEstimates().get(CardValue.FIVE));
        assertEquals(1L, business.groupEstimates().get(CardValue.EIGHT));

        business.finalizeResult(scrumMaster, CardValue.FIVE);
        assertTrue(issue.isApproved());
        assertEquals(5, issue.getValue());
    }

    @Test
    void developerCannotEstimateBeforeRoundIsReleased() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);

        assertThrows(RuntimeException.class,
                () -> business.estimate(developer, developer, CardValue.FIVE));
    }

    @Test
    void unknownDeveloperCannotEstimate() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer unknownDeveloper = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.releaseActiveIssue(scrumMaster);

        assertThrows(RuntimeException.class,
                () -> business.estimate(unknownDeveloper, unknownDeveloper, CardValue.FIVE));
    }

    @Test
    void developerCannotPerformScrumMasterActions() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());

        assertAll(
                () -> assertThrows(RuntimeException.class, () -> business.releaseActiveIssue(developer)),
                () -> assertThrows(RuntimeException.class, () -> business.selectIssue(developer, new Issue())),
                () -> assertThrows(RuntimeException.class, () -> business.reveal(developer, scrumMaster))
        );
    }

    @Test
    void developerCannotJoinAfterRoundIsReleased() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.releaseActiveIssue(scrumMaster);

        assertThrows(RuntimeException.class, () -> business.join(developer, developer));
    }

    @Test
    void developerCannotJoinTwice() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);

        assertThrows(RuntimeException.class, () -> business.join(developer, developer));
    }

    @Test
    void secondEstimateReplacesFirstEstimate() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);
        business.releaseActiveIssue(scrumMaster);

        business.estimate(developer, developer, CardValue.THREE);
        business.estimate(developer, developer, CardValue.EIGHT);

        assertEquals(CardValue.EIGHT, business.getDevelopersEstimated().get(developer));
    }

    @Test
    void progressShowsDeveloperWhoHasNotEstimatedYet() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer estimatedDeveloper = new Developer("Alex");
        Developer pendingDeveloper = new Developer("Kim");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(estimatedDeveloper, estimatedDeveloper);
        business.join(pendingDeveloper, pendingDeveloper);
        business.releaseActiveIssue(scrumMaster);
        business.estimate(estimatedDeveloper, estimatedDeveloper, CardValue.FIVE);

        assertFalse(business.allDevelopersEstimated());
        assertEquals(CardValue.FIVE, business.getDevelopersEstimated().get(estimatedDeveloper));
        assertNull(business.getDevelopersEstimated().get(pendingDeveloper));
    }

    @Test
    void scrumMasterCanRevealBeforeAllDevelopersEstimated() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);
        business.releaseActiveIssue(scrumMaster);

        assertDoesNotThrow(() -> business.reveal(scrumMaster, scrumMaster));
    }

    @Test
    void estimateValuesAreHiddenBeforeReveal() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);
        business.releaseActiveIssue(scrumMaster);
        business.estimate(developer, developer, CardValue.FIVE);

        assertThrows(IllegalStateException.class, business::getEstimateValues);
    }

    @Test
    void developerCannotEstimateAfterReveal() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);
        business.releaseActiveIssue(scrumMaster);
        business.reveal(scrumMaster, scrumMaster);

        assertThrows(IllegalStateException.class,
                () -> business.estimate(developer, developer, CardValue.FIVE));
    }

    @Test
    void otherScrumMasterCannotRevealOrFinalizeRound() {
        ScrumMaster owner = new ScrumMaster("Mara");
        ScrumMaster otherScrumMaster = new ScrumMaster("Sam");
        PlanningPokerBusiness business = createBusiness(owner, new Issue());
        business.releaseActiveIssue(owner);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> business.reveal(otherScrumMaster, otherScrumMaster)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> business.finalizeResult(otherScrumMaster, CardValue.FIVE))
        );
    }

    @Test
    void resultCannotBeFinalizedBeforeReveal() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());

        assertThrows(IllegalStateException.class,
                () -> business.finalizeResult(scrumMaster, CardValue.FIVE));
    }

    @Test
    void questionMarkAndCoffeeCanBeFinalized() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        PlanningPokerBusiness questionMarkBusiness = revealedBusiness(scrumMaster, new Issue());
        PlanningPokerBusiness coffeeBusiness = revealedBusiness(scrumMaster, new Issue());

        assertAll(
                () -> assertDoesNotThrow(
                        () -> questionMarkBusiness.finalizeResult(scrumMaster, CardValue.QUESTION_MARK)),
                () -> assertDoesNotThrow(
                        () -> coffeeBusiness.finalizeResult(scrumMaster, CardValue.COFFEE))
        );
    }

    @Test
    void newRoundForSameIssueResetsEstimates() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);
        business.releaseActiveIssue(scrumMaster);
        business.estimate(developer, developer, CardValue.FIVE);
        business.reveal(scrumMaster, scrumMaster);

        business.startNewRound(scrumMaster);

        assertNull(business.getDevelopersEstimated().get(developer));
    }

    @Test
    void activeIssueCanChangeOnlyBeforeRoundIsRevealed() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());

        assertDoesNotThrow(() -> business.selectIssue(scrumMaster, new Issue()));
        business.releaseActiveIssue(scrumMaster);
        business.reveal(scrumMaster, scrumMaster);

        assertThrows(IllegalStateException.class, () -> business.selectIssue(scrumMaster, new Issue()));
    }

    private PlanningPokerBusiness createBusiness(ScrumMaster scrumMaster, Issue issue) {
        PlanningPokerBusiness business = new PlanningPokerBusiness();
        business.createPlanningPoker(scrumMaster, 123L, issue);
        return business;
    }

    private PlanningPokerBusiness revealedBusiness(ScrumMaster scrumMaster, Issue issue) {
        PlanningPokerBusiness business = createBusiness(scrumMaster, issue);
        business.releaseActiveIssue(scrumMaster);
        business.reveal(scrumMaster, scrumMaster);
        return business;
    }
}
