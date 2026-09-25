package repository;

import business.PlanningPokerRepository;
import domain.PlanningPoker;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryPlanningPokerRepository implements PlanningPokerRepository {
    private final Map<UUID, PlanningPoker> planningPokers = new ConcurrentHashMap<>();

    @Override
    public void save(PlanningPoker planningPoker) {
        planningPokers.put(planningPoker.getId(), planningPoker);
    }

    @Override
    public PlanningPoker getById(UUID planningPokerId) {
        PlanningPoker planningPoker = planningPokers.get(planningPokerId);
        if (planningPoker == null) {
            throw new IllegalArgumentException("planning poker session not found: " + planningPokerId);
        }
        return planningPoker;
    }
}
