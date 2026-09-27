package domain;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record EstimationProgress(
        String scrumMasterName,
        Set<Developer> estimatedDevelopers,
        Set<Developer> pendingDevelopers,
        Map<Developer, Instant> developerJoinedAt,
        boolean released,
        boolean revealed
) {
    public EstimationProgress {
        estimatedDevelopers = Set.copyOf(estimatedDevelopers);
        pendingDevelopers = Set.copyOf(pendingDevelopers);
        developerJoinedAt = Map.copyOf(developerJoinedAt);
    }
}
