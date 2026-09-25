package domain;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EstimationRound {
    private final Issue issue;
    private final Map<Developer, CardValue> estimates;
    private boolean released;
    private boolean revealed;

    public EstimationRound(Issue issue) {
        this.issue = issue;
        this.estimates = new HashMap<>();
    }

    public void release() {
        released = true;
    }

    public boolean isReleased(){
        return this.released;
    }

    public void estimate(Developer developer, CardValue cardValue) {
        // erst nach freigabe durch scrum master schätzung abgeben
        if (!released) {
            throw new IllegalStateException("issue has not been released yet");
        }
        if (revealed) {
            throw new IllegalStateException("estimates cannot be changed after reveal");
        }
        if (cardValue == null) {
            throw new IllegalArgumentException("a card value is required");
        }

        estimates.put(developer, cardValue);
    }

    public boolean allDevelopersEstimated(List<Developer> developers) {
        if(!this.isReleased())
            throw new RuntimeException("estimation round not released yet");

        return !developers.isEmpty() && estimates.keySet().containsAll(developers);
    }

    public void reveal(ScrumMaster scrumMaster, ScrumMaster owner) {
        if (!scrumMaster.equals(owner)) {
            throw new IllegalArgumentException("only the scrum master can reveal the estimates");
        }

        revealed = true;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public Map<Developer, CardValue> getEstimateValues() {
        if (!revealed) {
            throw new IllegalStateException("estimates are not revealed yet");
        }

        return Map.copyOf(estimates);
    }

    public Map<CardValue, Long> groupEstimates() {
        return estimates.values().stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        () -> new EnumMap<>(CardValue.class),
                        Collectors.counting()
                ));
    }

    public int calculateAverage() {
        return (int) estimates.values().stream()
                .filter(CardValue::isNumeric)
                .mapToInt(CardValue::getNumericValue)
                .average()
                .orElse(0);
    }

    public int findMostFrequentValue() {
        return groupEstimates().entrySet().stream()
                .filter(entry -> entry.getKey().isNumeric())
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey().getNumericValue())
                .orElse(0);
    }

    public Set<Developer> getEstimatedDevelopers() {
        return Set.copyOf(estimates.keySet());
    }

    public void finalizeResult(ScrumMaster scrumMaster, CardValue value) {
        if (!this.isRevealed()) {
            throw new IllegalStateException("estimation round has not been revealed yet");
        }

        this.issue.approve(value);
    }
}
