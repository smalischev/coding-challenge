package domain;

import java.time.Instant;
import java.util.Map;

public record CompletedRound(
        int roundNumber,
        long gitlabIssueIid,
        String issueTitle,
        Instant completedAt,
        Map<String, CardValue> estimates
) {
    public CompletedRound {
        estimates = Map.copyOf(estimates);
    }
}
