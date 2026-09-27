package webservice;

import business.AllDevelopersEstimated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class EstimationCompletionNotifier {
    private final Map<UUID, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void subscribe(UUID planningPokerId, Sse sse, SseEventSink eventSink, boolean sessionOwner) {
        Subscription subscription = subscriptions.computeIfAbsent(planningPokerId,
                ignored -> new Subscription(sse, sse.newBroadcaster(), sse.newBroadcaster()));
        if (sessionOwner) {
            subscription.sessionOwners().register(eventSink);
        } else {
            subscription.otherSubscribers().register(eventSink);
        }
    }

    void notifyScrumMaster(@Observes AllDevelopersEstimated event) {
        broadcast(event.planningPokerId(), "all-developers-estimated", Map.of("allDevelopersEstimated", true), true);
    }

    public void broadcast(UUID planningPokerId, String name, Object payload) {
        broadcast(planningPokerId, name, payload, false);
    }

    private void broadcast(UUID planningPokerId, String name, Object payload, boolean ownersOnly) {
        Subscription subscription = subscriptions.get(planningPokerId);
        if (subscription == null) return;
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        var event = subscription.sse().newEventBuilder().name(name)
                .mediaType(MediaType.APPLICATION_JSON_TYPE).data(String.class, json).build();
        subscription.sessionOwners().broadcast(event);
        if (!ownersOnly) {
            subscription.otherSubscribers().broadcast(event);
        }
    }

    private record Subscription(Sse sse, SseBroadcaster sessionOwners, SseBroadcaster otherSubscribers) {
    }
}
