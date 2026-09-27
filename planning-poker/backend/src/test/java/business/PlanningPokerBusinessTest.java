package business;

import domain.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;


class PlanningPokerBusinessTest {
    // Prüft, dass ein Entwickler erst nach Beitritt und Freigabe schätzen darf.
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

    // Prüft den vollständigen Ablauf von der Sitzung bis zur Ergebnisübernahme.
    @Test
    void happyPath() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer firstDeveloper = new Developer("Alex");
        Developer secondDeveloper = new Developer("Kim");
        Issue issue = new Issue();
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, issue);
        business.join(planningPokerId, firstDeveloper, firstDeveloper);
        business.join(planningPokerId, secondDeveloper, secondDeveloper);

        business.releaseActiveIssue(planningPokerId, scrumMaster);
        business.estimate(planningPokerId, firstDeveloper, firstDeveloper, CardValue.FIVE);

        EstimationProgress estimationProgress = business.getEstimationProgress(planningPokerId);
        assertTrue(estimationProgress.estimatedDevelopers().contains(firstDeveloper));
        assertTrue(estimationProgress.pendingDevelopers().contains(secondDeveloper));
        assertFalse(business.allDevelopersEstimated(planningPokerId));

        business.estimate(planningPokerId, secondDeveloper, secondDeveloper, CardValue.EIGHT);
        assertTrue(business.allDevelopersEstimated(planningPokerId));

        business.reveal(planningPokerId, scrumMaster, scrumMaster);
        assertEquals(Map.of(firstDeveloper, CardValue.FIVE, secondDeveloper, CardValue.EIGHT),
                business.getEstimateValues(planningPokerId));
        assertEquals(6, business.calculateAverage(planningPokerId).orElseThrow());
        assertEquals(1L, business.groupEstimates(planningPokerId).get(CardValue.FIVE));
        assertEquals(1L, business.groupEstimates(planningPokerId).get(CardValue.EIGHT));

        business.finalizeResult(planningPokerId, scrumMaster, CardValue.FIVE);
        assertTrue(issue.isApproved());
        assertEquals(CardValue.FIVE, issue.getValue());
    }

    // Prüft, dass vor der Freigabe keine Schätzung abgegeben werden darf.
    @Test
    void developerCannotEstimateBeforeRoundIsReleased() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, developer, developer);

        assertThrows(RuntimeException.class,
                () -> business.estimate(planningPokerId, developer, developer, CardValue.FIVE));
    }

    // Prüft, dass nur beigetretene Entwickler schätzen dürfen.
    @Test
    void unknownDeveloperCannotEstimate() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer unknownDeveloper = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.releaseActiveIssue(planningPokerId, scrumMaster);

        assertThrows(RuntimeException.class,
                () -> business.estimate(planningPokerId, unknownDeveloper, unknownDeveloper, CardValue.FIVE));
    }

    // Prüft, dass Steuerungsaktionen ausschließlich dem Scrum Master vorbehalten sind.
    @Test
    void developerCannotPerformScrumMasterActions() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());

        assertAll(
                () -> assertThrows(NotAllowedException.class, () -> business.releaseActiveIssue(planningPokerId, developer)),
                () -> assertThrows(NotAllowedException.class, () -> business.selectIssue(planningPokerId, developer, new Issue())),
                () -> assertThrows(NotAllowedException.class, () -> business.startNewRound(planningPokerId, developer)),
                () -> assertThrows(NotAllowedException.class, () -> business.reveal(planningPokerId, developer, scrumMaster))
        );
    }

    // Prüft, dass der Scrum Master keine Entwickleraktionen ausführen darf.
    @Test
    void scrumMasterCannotPerformDeveloperActions() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());

        assertAll(
                () -> assertThrows(NotAllowedException.class, () -> business.join(planningPokerId, scrumMaster, developer)),
                () -> assertThrows(NotAllowedException.class,
                        () -> business.estimate(planningPokerId, scrumMaster, developer, CardValue.FIVE))
        );
    }

    // Prüft, dass ein Entwickler nicht im Namen eines anderen Entwicklers handeln darf.
    @Test
    void developerCannotJoinOrEstimateAsAnotherDeveloper() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer callingDeveloper = new Developer("Alex");
        Developer otherDeveloper = new Developer("Kim");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());

        assertThrows(NotAllowedException.class, () -> business.join(planningPokerId, callingDeveloper, otherDeveloper));

        business.join(planningPokerId, callingDeveloper, callingDeveloper);
        business.releaseActiveIssue(planningPokerId, scrumMaster);

        assertThrows(NotAllowedException.class,
                () -> business.estimate(planningPokerId, callingDeveloper, otherDeveloper, CardValue.FIVE));
    }

    // Prüft, dass unterschiedliche Entwicklerobjekte mit demselben Namen dieselbe Identität darstellen.
    @Test
    void developerWithSameNameCanJoinAndEstimate() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer callingDeveloper = new Developer("Alex");
        Developer joinedDeveloper = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());

        assertDoesNotThrow(() -> business.join(planningPokerId, callingDeveloper, joinedDeveloper));
        business.releaseActiveIssue(planningPokerId, scrumMaster);

        assertDoesNotThrow(() -> business.estimate(planningPokerId, callingDeveloper, joinedDeveloper, CardValue.FIVE));
    }

    // Prüft, dass ein erneuter Beitritt mit demselben Namen keine Änderung auslöst.
    @Test
    void developerWithDuplicateNameCanJoinIdempotently() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer firstDeveloper = new Developer("Alex");
        Developer duplicateDeveloper = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, firstDeveloper, firstDeveloper);

        assertDoesNotThrow(() -> business.join(planningPokerId, duplicateDeveloper, duplicateDeveloper));
    }

    // Prüft, dass nach der Freigabe keine weiteren Entwickler beitreten dürfen.
    @Test
    void developerCannotJoinAfterRoundIsReleased() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.releaseActiveIssue(planningPokerId, scrumMaster);

        assertThrows(RuntimeException.class, () -> business.join(planningPokerId, developer, developer));
    }

    // Prüft, dass ein erneuter Beitritt desselben Entwicklers keine Änderung auslöst.
    @Test
    void developerCanJoinTwiceIdempotently() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, developer, developer);

        assertDoesNotThrow(() -> business.join(planningPokerId, developer, developer));
    }

    // Prüft, dass ein bereits beigetretener Entwickler auch nach der Freigabe erneut beitreten kann.
    @Test
    void joinedDeveloperCanJoinIdempotentlyAfterRoundIsReleased() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, developer, developer);
        business.releaseActiveIssue(planningPokerId, scrumMaster);

        assertDoesNotThrow(() -> business.join(planningPokerId, developer, developer));
    }

    // Prüft, dass eine neue Schätzung desselben Entwicklers die vorherige ersetzt.
    @Test
    void secondEstimateReplacesFirstEstimate() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, developer, developer);
        business.releaseActiveIssue(planningPokerId, scrumMaster);

        business.estimate(planningPokerId, developer, developer, CardValue.THREE);
        business.estimate(planningPokerId, developer, developer, CardValue.EIGHT);

        business.reveal(planningPokerId, scrumMaster, scrumMaster);
        assertEquals(CardValue.EIGHT, business.getEstimateValues(planningPokerId).get(developer));
    }

    // Prüft, dass der Fortschritt abgestimmte und noch ausstehende Entwickler zeigt.
    @Test
    void progressShowsDeveloperWhoHasNotEstimatedYet() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer estimatedDeveloper = new Developer("Alex");
        Developer pendingDeveloper = new Developer("Kim");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, estimatedDeveloper, estimatedDeveloper);
        business.join(planningPokerId, pendingDeveloper, pendingDeveloper);
        business.releaseActiveIssue(planningPokerId, scrumMaster);
        business.estimate(planningPokerId, estimatedDeveloper, estimatedDeveloper, CardValue.FIVE);

        assertFalse(business.allDevelopersEstimated(planningPokerId));
        assertTrue(business.getEstimationProgress(planningPokerId).estimatedDevelopers().contains(estimatedDeveloper));
        assertTrue(business.getEstimationProgress(planningPokerId).pendingDevelopers().contains(pendingDeveloper));
    }

    // Prüft, dass der Scrum Master auch eine unvollständige Runde auflösen darf.
    @Test
    void scrumMasterCanRevealBeforeAllDevelopersEstimated() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, developer, developer);
        business.releaseActiveIssue(planningPokerId, scrumMaster);

        assertDoesNotThrow(() -> business.reveal(planningPokerId, scrumMaster, scrumMaster));
    }

    // Prüft, dass konkrete Kartenwerte vor der Auflösung verborgen bleiben.
    @Test
    void estimateValuesAreHiddenBeforeReveal() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, developer, developer);
        business.releaseActiveIssue(planningPokerId, scrumMaster);
        business.estimate(planningPokerId, developer, developer, CardValue.FIVE);

        assertThrows(IllegalStateException.class, () -> business.getEstimateValues(planningPokerId));
    }

    // Prüft, dass Auswertungen erst nach der Auflösung sichtbar sind.
    @Test
    void estimateStatisticsAreHiddenBeforeReveal() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, developer, developer);
        business.releaseActiveIssue(planningPokerId, scrumMaster);
        business.estimate(planningPokerId, developer, developer, CardValue.FIVE);

        assertAll(
                () -> assertThrows(IllegalStateException.class, () -> business.groupEstimates(planningPokerId)),
                () -> assertThrows(IllegalStateException.class, () -> business.calculateAverage(planningPokerId)),
                () -> assertThrows(IllegalStateException.class, () -> business.findMostFrequentValue(planningPokerId))
        );
    }

    // Prüft, dass nach der Auflösung keine weitere Schätzung möglich ist.
    @Test
    void developerCannotEstimateAfterReveal() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, developer, developer);
        business.releaseActiveIssue(planningPokerId, scrumMaster);
        business.reveal(planningPokerId, scrumMaster, scrumMaster);

        assertThrows(IllegalStateException.class,
                () -> business.estimate(planningPokerId, developer, developer, CardValue.FIVE));
    }

    // Prüft, dass nur der Besitzer der Sitzung Steuerungsaktionen ausführen darf.
    @Test
    void otherScrumMasterCannotPerformSessionOwnerActions() {
        ScrumMaster owner = new ScrumMaster("Mara");
        ScrumMaster otherScrumMaster = new ScrumMaster("Sam");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(owner, 123L, new Issue());

        assertAll(
                () -> assertThrows(NotAllowedException.class,
                        () -> business.selectIssue(planningPokerId, otherScrumMaster, new Issue())),
                () -> assertThrows(NotAllowedException.class,
                        () -> business.releaseActiveIssue(planningPokerId, otherScrumMaster)),
                () -> assertThrows(NotAllowedException.class,
                        () -> business.startNewRound(planningPokerId, otherScrumMaster)),
                () -> assertThrows(NotAllowedException.class,
                        () -> business.reveal(planningPokerId, otherScrumMaster, otherScrumMaster)),
                () -> assertThrows(NotAllowedException.class,
                        () -> business.finalizeResult(planningPokerId, otherScrumMaster, CardValue.FIVE))
        );
    }

    // Prüft, dass ein Ergebnis erst nach der Auflösung finalisiert werden darf.
    @Test
    void resultCannotBeFinalizedBeforeReveal() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());

        assertThrows(IllegalStateException.class,
                () -> business.finalizeResult(planningPokerId, scrumMaster, CardValue.FIVE));
    }

    // Prüft, dass Fragezeichen und Kaffeetasse als Ergebnis übernommen werden dürfen.
    @Test
    void questionMarkAndCoffeeCanBeFinalized() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        PlanningPokerBusiness questionMarkBusiness = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID questionMarkPlanningPokerId = questionMarkBusiness.createPlanningPoker(scrumMaster, 123L, new Issue());
        questionMarkBusiness.releaseActiveIssue(questionMarkPlanningPokerId, scrumMaster);
        questionMarkBusiness.reveal(questionMarkPlanningPokerId, scrumMaster, scrumMaster);
        PlanningPokerBusiness coffeeBusiness = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID coffeePlanningPokerId = coffeeBusiness.createPlanningPoker(scrumMaster, 123L, new Issue());
        coffeeBusiness.releaseActiveIssue(coffeePlanningPokerId, scrumMaster);
        coffeeBusiness.reveal(coffeePlanningPokerId, scrumMaster, scrumMaster);

        assertAll(
                () -> assertDoesNotThrow(
                        () -> questionMarkBusiness.finalizeResult(questionMarkPlanningPokerId, scrumMaster, CardValue.QUESTION_MARK)),
                () -> assertDoesNotThrow(
                        () -> coffeeBusiness.finalizeResult(coffeePlanningPokerId, scrumMaster, CardValue.COFFEE))
        );
    }

    // Prüft, dass eine neue Runde für dasselbe Issue die bisherigen Schätzungen leert.
    @Test
    void newRoundForSameIssueResetsEstimates() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, developer, developer);
        business.releaseActiveIssue(planningPokerId, scrumMaster);
        business.estimate(planningPokerId, developer, developer, CardValue.FIVE);
        business.reveal(planningPokerId, scrumMaster, scrumMaster);

        business.startNewRound(planningPokerId, scrumMaster);

        assertTrue(business.getEstimationProgress(planningPokerId).pendingDevelopers().contains(developer));
    }

    // Prüft, dass das aktive Issue nur vor der Auflösung gewechselt werden darf.
    @Test
    void activeIssueCanChangeOnlyBeforeRoundIsRevealed() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());

        assertDoesNotThrow(() -> business.selectIssue(planningPokerId, scrumMaster, new Issue()));
        business.releaseActiveIssue(planningPokerId, scrumMaster);
        business.reveal(planningPokerId, scrumMaster, scrumMaster);

        assertThrows(IllegalStateException.class, () -> business.selectIssue(planningPokerId, scrumMaster, new Issue()));
    }

    // Prüft, dass der häufigste numerische Kartenwert nach der Auflösung ermittelt wird.
    @Test
    void mostFrequentEstimateValueIsCalculated() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer firstDeveloper = new Developer("Alex");
        Developer secondDeveloper = new Developer("Kim");
        Developer thirdDeveloper = new Developer("Jo");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, firstDeveloper, firstDeveloper);
        business.join(planningPokerId, secondDeveloper, secondDeveloper);
        business.join(planningPokerId, thirdDeveloper, thirdDeveloper);
        business.releaseActiveIssue(planningPokerId, scrumMaster);
        business.estimate(planningPokerId, firstDeveloper, firstDeveloper, CardValue.FIVE);
        business.estimate(planningPokerId, secondDeveloper, secondDeveloper, CardValue.FIVE);
        business.estimate(planningPokerId, thirdDeveloper, thirdDeveloper, CardValue.EIGHT);
        business.reveal(planningPokerId, scrumMaster, scrumMaster);

        assertEquals(5, business.findMostFrequentValue(planningPokerId).orElseThrow());
    }

    // Prüft, dass Fragezeichen und Kaffeetasse nicht als numerische Auswertung gelten.
    @Test
    void statisticsAreEmptyWhenOnlySpecialCardsWereEstimated() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer firstDeveloper = new Developer("Alex");
        Developer secondDeveloper = new Developer("Kim");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, firstDeveloper, firstDeveloper);
        business.join(planningPokerId, secondDeveloper, secondDeveloper);
        business.releaseActiveIssue(planningPokerId, scrumMaster);
        business.estimate(planningPokerId, firstDeveloper, firstDeveloper, CardValue.QUESTION_MARK);
        business.estimate(planningPokerId, secondDeveloper, secondDeveloper, CardValue.COFFEE);
        business.reveal(planningPokerId, scrumMaster, scrumMaster);

        assertTrue(business.calculateAverage(planningPokerId).isEmpty());
        assertTrue(business.findMostFrequentValue(planningPokerId).isEmpty());
    }

    // Prüft, dass eine neue Runde für dasselbe Issue erst nach der Auflösung starten darf.
    @Test
    void newRoundCannotStartBeforeReveal() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());

        assertThrows(IllegalStateException.class, () -> business.startNewRound(planningPokerId, scrumMaster));
    }

    // Prüft, dass ein Issue-Wechsel die Schätzungen des zuvor aktiven Issues verwirft.
    @Test
    void issueChangeResetsEstimatesForNewActiveIssue() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, developer, developer);
        business.releaseActiveIssue(planningPokerId, scrumMaster);
        business.estimate(planningPokerId, developer, developer, CardValue.FIVE);

        business.selectIssue(planningPokerId, scrumMaster, new Issue());

        assertTrue(business.getEstimationProgress(planningPokerId).pendingDevelopers().contains(developer));
    }

    // Prüft, dass Fragezeichen und Kaffeetasse als gewähltes Ergebnis im Issue gespeichert werden.
    @Test
    void specialCardValuesAreStoredInIssue() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Issue questionMarkIssue = new Issue();
        Issue coffeeIssue = new Issue();
        PlanningPokerBusiness questionMarkBusiness = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID questionMarkPlanningPokerId = questionMarkBusiness.createPlanningPoker(scrumMaster, 123L, questionMarkIssue);
        questionMarkBusiness.releaseActiveIssue(questionMarkPlanningPokerId, scrumMaster);
        questionMarkBusiness.reveal(questionMarkPlanningPokerId, scrumMaster, scrumMaster);
        PlanningPokerBusiness coffeeBusiness = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID coffeePlanningPokerId = coffeeBusiness.createPlanningPoker(scrumMaster, 123L, coffeeIssue);
        coffeeBusiness.releaseActiveIssue(coffeePlanningPokerId, scrumMaster);
        coffeeBusiness.reveal(coffeePlanningPokerId, scrumMaster, scrumMaster);

        questionMarkBusiness.finalizeResult(questionMarkPlanningPokerId, scrumMaster, CardValue.QUESTION_MARK);
        coffeeBusiness.finalizeResult(coffeePlanningPokerId, scrumMaster, CardValue.COFFEE);

        assertEquals(CardValue.QUESTION_MARK, questionMarkIssue.getValue());
        assertEquals(CardValue.COFFEE, coffeeIssue.getValue());
    }

    // Prüft, dass jeder definierte Kartenwert als Schätzung abgegeben werden kann.
    @Test
    void everyCardValueCanBeEstimated() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        List<Developer> developers = new ArrayList<>();

        for (CardValue cardValue : CardValue.values()) {
            Developer developer = new Developer(cardValue.name());
            developers.add(developer);
            business.join(planningPokerId, developer, developer);
        }

        business.releaseActiveIssue(planningPokerId, scrumMaster);
        for (int index = 0; index < CardValue.values().length; index++) {
            Developer developer = developers.get(index);
            business.estimate(planningPokerId, developer, developer, CardValue.values()[index]);
        }
        business.reveal(planningPokerId, scrumMaster, scrumMaster);

        assertEquals(EnumSet.allOf(CardValue.class),
                EnumSet.copyOf(business.getEstimateValues(planningPokerId).values()));
    }

    // Prüft, dass eine Schätzung immer einen Wert aus dem Kartensatz enthalten muss.
    @Test
    void nullCardValueCannotBeEstimated() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());
        business.join(planningPokerId, developer, developer);
        business.releaseActiveIssue(planningPokerId, scrumMaster);

        assertThrows(IllegalArgumentException.class,
                () -> business.estimate(planningPokerId, developer, developer, null));
    }

    // Prüft, dass der beim Beitritt angegebene Name in der Teilnehmerübersicht erhalten bleibt.
    @Test
    void joinedDeveloperIsVisibleWithName() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        UUID planningPokerId = business.createPlanningPoker(scrumMaster, 123L, new Issue());

        business.join(planningPokerId, developer, developer);

        assertTrue(business.getEstimationProgress(planningPokerId).pendingDevelopers().stream()
                .map(Developer::getName)
                .anyMatch("Alex"::equals));
    }

    // Prüft, dass mehrere Sitzungen unabhängig voneinander im Repository verwaltet werden.
    @Test
    void sessionsAreManagedIndependently() {
        PlanningPokerBusiness business = new PlanningPokerBusiness((projectId, issueIid, label) -> { });
        ScrumMaster firstScrumMaster = new ScrumMaster("Mara");
        ScrumMaster secondScrumMaster = new ScrumMaster("Sam");
        Developer firstDeveloper = new Developer("Alex");
        Developer secondDeveloper = new Developer("Kim");

        UUID firstSessionId = business.createPlanningPoker(firstScrumMaster, 1L, new Issue(1L));
        UUID secondSessionId = business.createPlanningPoker(secondScrumMaster, 2L, new Issue(2L));

        business.join(firstSessionId, firstDeveloper, firstDeveloper);
        business.releaseActiveIssue(firstSessionId, firstScrumMaster);
        business.estimate(firstSessionId, firstDeveloper, firstDeveloper, CardValue.THREE);

        business.join(secondSessionId, secondDeveloper, secondDeveloper);

        assertTrue(business.getEstimationProgress(firstSessionId).estimatedDevelopers().contains(firstDeveloper));
        assertTrue(business.getEstimationProgress(secondSessionId).pendingDevelopers().contains(secondDeveloper));
    }

}
