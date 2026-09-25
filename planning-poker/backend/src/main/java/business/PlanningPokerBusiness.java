package business;

import domain.*;

import java.util.Map;

public class PlanningPokerBusiness {
    private PlanningPoker planningPoker;

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

    public Map<Developer, CardValue> getDevelopersEstimated(){
        return planningPoker.getDevelopersEstimated();
    }

    // (B) wert in das Gitlab-Issue übernehmen
    public void takeToGitlab(){
        //TODO: scrum master wählt vereinbarten Wert und bestätigt die Runde
        //TODO: Wert wird dem Gitlab-Issue hinterlegt - wie und in welcher Form ist Teil der Aufgabe; in readme dokumentieren
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

    public int calculateAverage() {
        return planningPoker.calculateAverage();
    }

    public int findMostFrequentValue() {
        return planningPoker.findMostFrequentValue();
    }

    public void finalizeResult(ScrumMaster scrumMaster, CardValue value) {
        planningPoker.finalizeResult(scrumMaster, value);
    }
}
