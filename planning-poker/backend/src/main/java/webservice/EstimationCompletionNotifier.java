package webservice;

import business.AllDevelopersEstimated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class EstimationCompletionNotifier {
    private final Map<UUID, Subscription> subscriptions = new ConcurrentHashMap<>();

    public void subscribe(UUID planningPokerId, Sse sse, SseEventSink eventSink) {
        Subscription subscription = subscriptions.computeIfAbsent(planningPokerId,
                ignored -> new Subscription(sse, sse.newBroadcaster()));
        subscription.broadcaster().register(eventSink);
    }

    void notifyScrumMaster(@Observes AllDevelopersEstimated event) {
        Subscription subscription = subscriptions.get(event.planningPokerId());
        if (subscription == null) {
            return;
        }

        subscription.broadcaster().broadcast(subscription.sse().newEventBuilder()
                .name("all-developers-estimated")
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .data(String.class, "{\"allDevelopersEstimated\":true}")
                .build());
    }

    private record Subscription(Sse sse, SseBroadcaster broadcaster) {
    }
}
