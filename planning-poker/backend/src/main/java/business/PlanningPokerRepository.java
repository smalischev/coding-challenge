package business;

import domain.PlanningPoker;

import java.util.UUID;

public interface PlanningPokerRepository {
    void save(PlanningPoker planningPoker);

    PlanningPoker getById(UUID planningPokerId);
}
