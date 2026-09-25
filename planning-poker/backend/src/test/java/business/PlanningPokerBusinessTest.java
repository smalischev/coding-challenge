package business;

import domain.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

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
        PlanningPokerBusiness business = new PlanningPokerBusiness();

        business.createPlanningPoker(scrumMaster, 123L, issue);
        business.join(firstDeveloper, firstDeveloper);
        business.join(secondDeveloper, secondDeveloper);

        business.releaseActiveIssue(scrumMaster);
        business.estimate(firstDeveloper, firstDeveloper, CardValue.FIVE);

        EstimationProgress estimationProgress = business.getEstimationProgress();
        assertTrue(estimationProgress.estimatedDevelopers().contains(firstDeveloper));
        assertTrue(estimationProgress.pendingDevelopers().contains(secondDeveloper));
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
        assertEquals(CardValue.FIVE, issue.getValue());
    }

    // Prüft, dass vor der Freigabe keine Schätzung abgegeben werden darf.
    @Test
    void developerCannotEstimateBeforeRoundIsReleased() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);

        assertThrows(RuntimeException.class,
                () -> business.estimate(developer, developer, CardValue.FIVE));
    }

    // Prüft, dass nur beigetretene Entwickler schätzen dürfen.
    @Test
    void unknownDeveloperCannotEstimate() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer unknownDeveloper = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.releaseActiveIssue(scrumMaster);

        assertThrows(RuntimeException.class,
                () -> business.estimate(unknownDeveloper, unknownDeveloper, CardValue.FIVE));
    }

    // Prüft, dass Steuerungsaktionen ausschließlich dem Scrum Master vorbehalten sind.
    @Test
    void developerCannotPerformScrumMasterActions() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());

        assertAll(
                () -> assertThrows(NotAllowedException.class, () -> business.releaseActiveIssue(developer)),
                () -> assertThrows(NotAllowedException.class, () -> business.selectIssue(developer, new Issue())),
                () -> assertThrows(NotAllowedException.class, () -> business.startNewRound(developer)),
                () -> assertThrows(NotAllowedException.class, () -> business.reveal(developer, scrumMaster))
        );
    }

    // Prüft, dass der Scrum Master keine Entwickleraktionen ausführen darf.
    @Test
    void scrumMasterCannotPerformDeveloperActions() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());

        assertAll(
                () -> assertThrows(NotAllowedException.class, () -> business.join(scrumMaster, developer)),
                () -> assertThrows(NotAllowedException.class,
                        () -> business.estimate(scrumMaster, developer, CardValue.FIVE))
        );
    }

    // Prüft, dass ein Entwickler nicht im Namen eines anderen Entwicklers handeln darf.
    @Test
    void developerCannotJoinOrEstimateAsAnotherDeveloper() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer callingDeveloper = new Developer("Alex");
        Developer otherDeveloper = new Developer("Kim");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());

        assertThrows(NotAllowedException.class, () -> business.join(callingDeveloper, otherDeveloper));

        business.join(callingDeveloper, callingDeveloper);
        business.releaseActiveIssue(scrumMaster);

        assertThrows(NotAllowedException.class,
                () -> business.estimate(callingDeveloper, otherDeveloper, CardValue.FIVE));
    }

    // Prüft, dass unterschiedliche Entwicklerobjekte mit demselben Namen dieselbe Identität darstellen.
    @Test
    void developerWithSameNameCanJoinAndEstimate() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer callingDeveloper = new Developer("Alex");
        Developer joinedDeveloper = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());

        assertDoesNotThrow(() -> business.join(callingDeveloper, joinedDeveloper));
        business.releaseActiveIssue(scrumMaster);

        assertDoesNotThrow(() -> business.estimate(callingDeveloper, joinedDeveloper, CardValue.FIVE));
    }

    // Prüft, dass kein zweiter Entwickler mit einem bereits verwendeten Namen beitreten darf.
    @Test
    void developerWithDuplicateNameCannotJoin() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer firstDeveloper = new Developer("Alex");
        Developer duplicateDeveloper = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(firstDeveloper, firstDeveloper);

        assertThrows(RuntimeException.class, () -> business.join(duplicateDeveloper, duplicateDeveloper));
    }

    // Prüft, dass nach der Freigabe keine weiteren Entwickler beitreten dürfen.
    @Test
    void developerCannotJoinAfterRoundIsReleased() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.releaseActiveIssue(scrumMaster);

        assertThrows(RuntimeException.class, () -> business.join(developer, developer));
    }

    // Prüft, dass ein Entwickler nur einmal an der Sitzung teilnehmen kann.
    @Test
    void developerCannotJoinTwice() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);

        assertThrows(RuntimeException.class, () -> business.join(developer, developer));
    }

    // Prüft, dass eine neue Schätzung desselben Entwicklers die vorherige ersetzt.
    @Test
    void secondEstimateReplacesFirstEstimate() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);
        business.releaseActiveIssue(scrumMaster);

        business.estimate(developer, developer, CardValue.THREE);
        business.estimate(developer, developer, CardValue.EIGHT);

        business.reveal(scrumMaster, scrumMaster);
        assertEquals(CardValue.EIGHT, business.getEstimateValues().get(developer));
    }

    // Prüft, dass der Fortschritt abgestimmte und noch ausstehende Entwickler zeigt.
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
        assertTrue(business.getEstimationProgress().estimatedDevelopers().contains(estimatedDeveloper));
        assertTrue(business.getEstimationProgress().pendingDevelopers().contains(pendingDeveloper));
    }

    // Prüft, dass der Scrum Master auch eine unvollständige Runde auflösen darf.
    @Test
    void scrumMasterCanRevealBeforeAllDevelopersEstimated() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);
        business.releaseActiveIssue(scrumMaster);

        assertDoesNotThrow(() -> business.reveal(scrumMaster, scrumMaster));
    }

    // Prüft, dass konkrete Kartenwerte vor der Auflösung verborgen bleiben.
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

    // Prüft, dass Auswertungen erst nach der Auflösung sichtbar sind.
    @Test
    void estimateStatisticsAreHiddenBeforeReveal() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);
        business.releaseActiveIssue(scrumMaster);
        business.estimate(developer, developer, CardValue.FIVE);

        assertAll(
                () -> assertThrows(IllegalStateException.class, business::groupEstimates),
                () -> assertThrows(IllegalStateException.class, business::calculateAverage),
                () -> assertThrows(IllegalStateException.class, business::findMostFrequentValue)
        );
    }

    // Prüft, dass nach der Auflösung keine weitere Schätzung möglich ist.
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

    // Prüft, dass nur der Besitzer der Sitzung Steuerungsaktionen ausführen darf.
    @Test
    void otherScrumMasterCannotPerformSessionOwnerActions() {
        ScrumMaster owner = new ScrumMaster("Mara");
        ScrumMaster otherScrumMaster = new ScrumMaster("Sam");
        PlanningPokerBusiness business = createBusiness(owner, new Issue());

        assertAll(
                () -> assertThrows(NotAllowedException.class,
                        () -> business.selectIssue(otherScrumMaster, new Issue())),
                () -> assertThrows(NotAllowedException.class,
                        () -> business.releaseActiveIssue(otherScrumMaster)),
                () -> assertThrows(NotAllowedException.class,
                        () -> business.startNewRound(otherScrumMaster)),
                () -> assertThrows(NotAllowedException.class,
                        () -> business.reveal(otherScrumMaster, otherScrumMaster)),
                () -> assertThrows(NotAllowedException.class,
                        () -> business.finalizeResult(otherScrumMaster, CardValue.FIVE))
        );
    }

    // Prüft, dass ein Ergebnis erst nach der Auflösung finalisiert werden darf.
    @Test
    void resultCannotBeFinalizedBeforeReveal() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());

        assertThrows(IllegalStateException.class,
                () -> business.finalizeResult(scrumMaster, CardValue.FIVE));
    }

    // Prüft, dass Fragezeichen und Kaffeetasse als Ergebnis übernommen werden dürfen.
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

    // Prüft, dass eine neue Runde für dasselbe Issue die bisherigen Schätzungen leert.
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

        assertTrue(business.getEstimationProgress().pendingDevelopers().contains(developer));
    }

    // Prüft, dass das aktive Issue nur vor der Auflösung gewechselt werden darf.
    @Test
    void activeIssueCanChangeOnlyBeforeRoundIsRevealed() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());

        assertDoesNotThrow(() -> business.selectIssue(scrumMaster, new Issue()));
        business.releaseActiveIssue(scrumMaster);
        business.reveal(scrumMaster, scrumMaster);

        assertThrows(IllegalStateException.class, () -> business.selectIssue(scrumMaster, new Issue()));
    }

    // Prüft, dass der häufigste numerische Kartenwert nach der Auflösung ermittelt wird.
    @Test
    void mostFrequentEstimateValueIsCalculated() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer firstDeveloper = new Developer("Alex");
        Developer secondDeveloper = new Developer("Kim");
        Developer thirdDeveloper = new Developer("Jo");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(firstDeveloper, firstDeveloper);
        business.join(secondDeveloper, secondDeveloper);
        business.join(thirdDeveloper, thirdDeveloper);
        business.releaseActiveIssue(scrumMaster);
        business.estimate(firstDeveloper, firstDeveloper, CardValue.FIVE);
        business.estimate(secondDeveloper, secondDeveloper, CardValue.FIVE);
        business.estimate(thirdDeveloper, thirdDeveloper, CardValue.EIGHT);
        business.reveal(scrumMaster, scrumMaster);

        assertEquals(5, business.findMostFrequentValue());
    }

    // Prüft, dass eine neue Runde für dasselbe Issue erst nach der Auflösung starten darf.
    @Test
    void newRoundCannotStartBeforeReveal() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());

        assertThrows(IllegalStateException.class, () -> business.startNewRound(scrumMaster));
    }

    // Prüft, dass ein Issue-Wechsel die Schätzungen des zuvor aktiven Issues verwirft.
    @Test
    void issueChangeResetsEstimatesForNewActiveIssue() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);
        business.releaseActiveIssue(scrumMaster);
        business.estimate(developer, developer, CardValue.FIVE);

        business.selectIssue(scrumMaster, new Issue());

        assertTrue(business.getEstimationProgress().pendingDevelopers().contains(developer));
    }

    // Prüft, dass Fragezeichen und Kaffeetasse als gewähltes Ergebnis im Issue gespeichert werden.
    @Test
    void specialCardValuesAreStoredInIssue() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Issue questionMarkIssue = new Issue();
        Issue coffeeIssue = new Issue();
        PlanningPokerBusiness questionMarkBusiness = revealedBusiness(scrumMaster, questionMarkIssue);
        PlanningPokerBusiness coffeeBusiness = revealedBusiness(scrumMaster, coffeeIssue);

        questionMarkBusiness.finalizeResult(scrumMaster, CardValue.QUESTION_MARK);
        coffeeBusiness.finalizeResult(scrumMaster, CardValue.COFFEE);

        assertEquals(CardValue.QUESTION_MARK, questionMarkIssue.getValue());
        assertEquals(CardValue.COFFEE, coffeeIssue.getValue());
    }

    // Prüft, dass jeder definierte Kartenwert als Schätzung abgegeben werden kann.
    @Test
    void everyCardValueCanBeEstimated() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        List<Developer> developers = new ArrayList<>();

        for (CardValue cardValue : CardValue.values()) {
            Developer developer = new Developer(cardValue.name());
            developers.add(developer);
            business.join(developer, developer);
        }

        business.releaseActiveIssue(scrumMaster);
        for (int index = 0; index < CardValue.values().length; index++) {
            Developer developer = developers.get(index);
            business.estimate(developer, developer, CardValue.values()[index]);
        }
        business.reveal(scrumMaster, scrumMaster);

        assertEquals(EnumSet.allOf(CardValue.class),
                EnumSet.copyOf(business.getEstimateValues().values()));
    }

    // Prüft, dass eine Schätzung immer einen Wert aus dem Kartensatz enthalten muss.
    @Test
    void nullCardValueCannotBeEstimated() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());
        business.join(developer, developer);
        business.releaseActiveIssue(scrumMaster);

        assertThrows(IllegalArgumentException.class,
                () -> business.estimate(developer, developer, null));
    }

    // Prüft, dass der beim Beitritt angegebene Name in der Teilnehmerübersicht erhalten bleibt.
    @Test
    void joinedDeveloperIsVisibleWithName() {
        ScrumMaster scrumMaster = new ScrumMaster("Mara");
        Developer developer = new Developer("Alex");
        PlanningPokerBusiness business = createBusiness(scrumMaster, new Issue());

        business.join(developer, developer);

        assertTrue(business.getEstimationProgress().pendingDevelopers().stream()
                .map(Developer::getName)
                .anyMatch("Alex"::equals));
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
