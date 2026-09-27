package domain;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record EstimationProgress(
        Set<Developer> estimatedDevelopers,
        Set<Developer> pendingDevelopers,
        Map<Developer, Instant> developerJoinedAt
) {
    public EstimationProgress {
        estimatedDevelopers = Set.copyOf(estimatedDevelopers);
        pendingDevelopers = Set.copyOf(pendingDevelopers);
        developerJoinedAt = Map.copyOf(developerJoinedAt);
    }
}
