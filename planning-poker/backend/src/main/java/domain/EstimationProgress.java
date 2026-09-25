package domain;

import java.util.Set;

public record EstimationProgress(
        Set<Developer> estimatedDevelopers,
        Set<Developer> pendingDevelopers
) {
    public EstimationProgress {
        estimatedDevelopers = Set.copyOf(estimatedDevelopers);
        pendingDevelopers = Set.copyOf(pendingDevelopers);
    }
}
