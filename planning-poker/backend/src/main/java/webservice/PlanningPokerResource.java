package webservice;

import business.PlanningPokerBusiness;
import auth.webservice.AuthenticatedMemberFactory;
import domain.CardValue;
import domain.Developer;
import domain.EstimationProgress;
import domain.Issue;
import domain.ScrumMaster;
import jakarta.inject.Inject;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Path("/planning-pokers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Planning Poker", description = "Planning-Poker-Sessions und ihre aktiven Schätzrunden")
@SecurityScheme(
        securitySchemeName = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT aus dem Login-Endpunkt"
)
@SecurityRequirement(name = "bearerAuth")
public class PlanningPokerResource {
    @Inject
    PlanningPokerBusiness planningPokerBusiness;
    @Inject
    AuthenticatedMemberFactory authenticatedMemberFactory;
    @Inject
    EstimationCompletionNotifier estimationCompletionNotifier;

    @POST
    @RolesAllowed("SCRUM_MASTER")
    @Operation(summary = "Session erstellen", description = "Erstellt eine Planning-Poker-Session mit dem angegebenen GitLab-Issue als aktivem Issue. Nur Scrum Master dürfen Sessions erstellen.")
    @APIResponse(responseCode = "201", description = "Session wurde erstellt")
    @APIResponse(responseCode = "403", description = "Der angemeldete Benutzer ist kein Scrum Master")
    public Response createSession(CreateSessionRequest request) {
        Issue issue = planningPokerBusiness.loadIssue(request.gitlabProjectId(), request.gitlabIssueIid());
        UUID planningPokerId = planningPokerBusiness.createPlanningPoker(
                (ScrumMaster) authenticatedMemberFactory.create(),
                request.gitlabProjectId(),
                issue
        );

        return Response.status(Response.Status.CREATED)
                .entity(new CreateSessionResponse(planningPokerId))
                .build();
    }

    @POST
    @Path("/{planningPokerId}/developers")
    @RolesAllowed("DEVELOPER")
    @Operation(summary = "Session beitreten", description = "Fügt den angemeldeten Developer der Session hinzu. Nach Freigabe der Runde sind keine weiteren Beitritte möglich.")
    @APIResponse(responseCode = "204", description = "Developer ist der Session beigetreten")
    @APIResponse(responseCode = "403", description = "Der angemeldete Benutzer ist kein Developer")
    public Response join(@PathParam("planningPokerId") UUID planningPokerId, DeveloperRequest request) {
        Developer developer = (Developer) authenticatedMemberFactory.create();
        planningPokerBusiness.join(planningPokerId, developer, developer);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{planningPokerId}/active-issue")
    @RolesAllowed("SCRUM_MASTER")
    @Operation(summary = "Aktives Issue wählen", description = "Wählt das Issue für die aktive Schätzrunde. Der Owner darf es nur vor dem Aufdecken der Runde wechseln.")
    @APIResponse(responseCode = "204", description = "Aktives Issue wurde gewählt")
    @APIResponse(responseCode = "403", description = "Der angemeldete Benutzer ist nicht der Scrum-Master-Owner der Session")
    public Response selectIssue(@PathParam("planningPokerId") UUID planningPokerId, SelectIssueRequest request) {
        Issue issue = planningPokerBusiness.loadIssue(planningPokerId, request.gitlabIssueIid());
        planningPokerBusiness.selectIssue(
                planningPokerId,
                authenticatedMemberFactory.create(),
                issue
        );
        return Response.noContent().build();
    }

    @GET
    @Path("/{planningPokerId}/active-issue")
    @RolesAllowed({"SCRUM_MASTER", "DEVELOPER"})
    @Operation(summary = "Aktives Issue abrufen", description = "Liefert IID, Titel und Beschreibung des aktuell ausgewählten GitLab-Issues.")
    @APIResponse(responseCode = "200", description = "Aktives Issue")
    public ActiveIssueResponse getActiveIssue(@PathParam("planningPokerId") UUID planningPokerId) {
        Issue issue = planningPokerBusiness.getActiveIssue(planningPokerId);
        return new ActiveIssueResponse(issue.getGitlabIssueIid(), issue.getTitle(), issue.getDescription());
    }

    @POST
    @Path("/{planningPokerId}/active-issue/release")
    @RolesAllowed("SCRUM_MASTER")
    @Operation(summary = "Aktives Issue freigeben", description = "Gibt das aktive Issue zur verdeckten Schätzung frei und schließt die Teilnehmerliste.")
    @APIResponse(responseCode = "204", description = "Issue wurde freigegeben")
    public Response releaseActiveIssue(@PathParam("planningPokerId") UUID planningPokerId, ScrumMasterRequest request) {
        planningPokerBusiness.releaseActiveIssue(planningPokerId, authenticatedMemberFactory.create());
        return Response.noContent().build();
    }

    @POST
    @Path("/{planningPokerId}/active-round/estimates")
    @RolesAllowed("DEVELOPER")
    @Operation(summary = "Schätzung abgeben", description = "Speichert oder ersetzt die verdeckte Schätzung des angemeldeten Developers für die aktive, freigegebene Runde.")
    @APIResponse(responseCode = "204", description = "Schätzung wurde gespeichert")
    @APIResponse(responseCode = "403", description = "Der angemeldete Benutzer ist kein Developer")
    public Response estimate(@PathParam("planningPokerId") UUID planningPokerId, EstimateRequest request) {
        Developer developer = (Developer) authenticatedMemberFactory.create();
        planningPokerBusiness.estimate(planningPokerId, developer, developer, request.value());
        return Response.noContent().build();
    }

    @GET
    @Path("/{planningPokerId}/active-round/progress")
    @RolesAllowed({"SCRUM_MASTER", "DEVELOPER"})
    @Operation(summary = "Abstimmungsfortschritt abrufen", description = "Liefert geschätzte und noch ausstehende Developer, ohne Kartenwerte offenzulegen.")
    @APIResponse(responseCode = "200", description = "Abstimmungsfortschritt")
    public EstimationProgressResponse getEstimationProgress(@PathParam("planningPokerId") UUID planningPokerId) {
        EstimationProgress progress = planningPokerBusiness.getEstimationProgress(planningPokerId);
        return new EstimationProgressResponse(
                namesOf(progress.estimatedDevelopers()),
                namesOf(progress.pendingDevelopers()),
                progress.developerJoinedAt().entrySet().stream()
                        .sorted(Map.Entry.comparingByValue())
                        .map(entry -> new DeveloperProgressResponse(
                                entry.getKey().getName(),
                                entry.getValue().toString(),
                                progress.estimatedDevelopers().contains(entry.getKey())
                        ))
                        .toList()
        );
    }

    @GET
    @Path("/{planningPokerId}/active-round/all-developers-estimated")
    @RolesAllowed({"SCRUM_MASTER", "DEVELOPER"})
    @Operation(summary = "Vollständigkeit der Schätzungen prüfen", description = "Gibt an, ob alle Developer der freigegebenen Runde geschätzt haben.")
    @APIResponse(responseCode = "200", description = "Status der Schätzrunde")
    public AllDevelopersEstimatedResponse allDevelopersEstimated(@PathParam("planningPokerId") UUID planningPokerId) {
        return new AllDevelopersEstimatedResponse(planningPokerBusiness.allDevelopersEstimated(planningPokerId));
    }

    @GET
    @Path("/{planningPokerId}/active-round/events")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RolesAllowed("SCRUM_MASTER")
    @Operation(summary = "Benachrichtigungen abonnieren", description = "Öffnet einen Server-Sent-Events-Stream für den Session-Owner. Sobald alle Developer geschätzt haben, wird das Ereignis all-developers-estimated mit dem JSON-Wert {\"allDevelopersEstimated\":true} gesendet.")
    @APIResponse(responseCode = "200", description = "Offener SSE-Stream")
    @APIResponse(responseCode = "403", description = "Der angemeldete Scrum Master ist nicht Owner der Session")
    public void subscribeToEstimationCompletion(@PathParam("planningPokerId") UUID planningPokerId,
                                                @Context Sse sse,
                                                @Context SseEventSink eventSink) {
        ScrumMaster scrumMaster = (ScrumMaster) authenticatedMemberFactory.create();
        if (!planningPokerBusiness.isSessionOwner(planningPokerId, scrumMaster)) {
            throw new ForbiddenException();
        }
        estimationCompletionNotifier.subscribe(planningPokerId, sse, eventSink);
    }

    @POST
    @Path("/{planningPokerId}/active-round/reveal")
    @RolesAllowed("SCRUM_MASTER")
    @Operation(summary = "Schätzrunde aufdecken", description = "Deckt die Runde jederzeit auf, auch wenn noch nicht alle Developer geschätzt haben.")
    @APIResponse(responseCode = "204", description = "Schätzrunde wurde aufgedeckt")
    public Response reveal(@PathParam("planningPokerId") UUID planningPokerId, ScrumMasterRequest request) {
        ScrumMaster scrumMaster = (ScrumMaster) authenticatedMemberFactory.create();
        planningPokerBusiness.reveal(planningPokerId, scrumMaster, scrumMaster);
        return Response.noContent().build();
    }

    @GET
    @Path("/{planningPokerId}/active-round/estimates")
    @RolesAllowed({"SCRUM_MASTER", "DEVELOPER"})
    @Operation(summary = "Einzelschätzungen abrufen", description = "Liefert die Schätzkarten je Developer. Erst nach dem Aufdecken verfügbar.")
    @APIResponse(responseCode = "200", description = "Aufgedeckte Einzelschätzungen")
    public List<EstimateValueResponse> getEstimateValues(@PathParam("planningPokerId") UUID planningPokerId) {
        return planningPokerBusiness.getEstimateValues(planningPokerId).entrySet().stream()
                .map(entry -> new EstimateValueResponse(entry.getKey().getName(), entry.getValue()))
                .toList();
    }

    @GET
    @Path("/{planningPokerId}/active-round/estimate-groups")
    @RolesAllowed({"SCRUM_MASTER", "DEVELOPER"})
    @Operation(summary = "Schätzungen gruppieren", description = "Liefert die Anzahl der abgegebenen Karten je CardValue. Erst nach dem Aufdecken verfügbar.")
    @APIResponse(responseCode = "200", description = "Gruppierte Schätzungen")
    public Map<CardValue, Long> groupEstimates(@PathParam("planningPokerId") UUID planningPokerId) {
        return planningPokerBusiness.groupEstimates(planningPokerId);
    }

    @GET
    @Path("/{planningPokerId}/active-round/average")
    @RolesAllowed({"SCRUM_MASTER", "DEVELOPER"})
    @Operation(summary = "Durchschnitt berechnen", description = "Liefert den Durchschnitt der numerischen Kartenwerte. Fragezeichen und Kaffeetasse werden nicht einbezogen.")
    @APIResponse(responseCode = "200", description = "Durchschnitt oder null, wenn keine numerische Karte vorliegt")
    public NumericEstimationResponse calculateAverage(@PathParam("planningPokerId") UUID planningPokerId) {
        return new NumericEstimationResponse(
                planningPokerBusiness.calculateAverage(planningPokerId).stream().boxed().findFirst().orElse(null)
        );
    }

    @GET
    @Path("/{planningPokerId}/active-round/most-frequent-value")
    @RolesAllowed({"SCRUM_MASTER", "DEVELOPER"})
    @Operation(summary = "Häufigsten Wert bestimmen", description = "Liefert den häufigsten numerischen Kartenwert. Erst nach dem Aufdecken verfügbar.")
    @APIResponse(responseCode = "200", description = "Häufigster Wert oder null, wenn keine numerische Karte vorliegt")
    public NumericEstimationResponse findMostFrequentValue(@PathParam("planningPokerId") UUID planningPokerId) {
        return new NumericEstimationResponse(
                planningPokerBusiness.findMostFrequentValue(planningPokerId).stream().boxed().findFirst().orElse(null)
        );
    }

    @POST
    @Path("/{planningPokerId}/active-round")
    @RolesAllowed("SCRUM_MASTER")
    @Operation(summary = "Neue Schätzrunde starten", description = "Startet nach dem Aufdecken eine neue Runde für dasselbe aktive Issue und setzt alle Karten zurück.")
    @APIResponse(responseCode = "204", description = "Neue Schätzrunde wurde gestartet")
    public Response startNewRound(@PathParam("planningPokerId") UUID planningPokerId, ScrumMasterRequest request) {
        planningPokerBusiness.startNewRound(planningPokerId, authenticatedMemberFactory.create());
        return Response.noContent().build();
    }

    @POST
    @Path("/{planningPokerId}/active-round/result")
    @RolesAllowed("SCRUM_MASTER")
    @Operation(summary = "Ergebnis in GitLab übernehmen", description = "Bestätigt eine Karte aus dem definierten Kartensatz und speichert sie als scoped Label am GitLab-Issue. Erst nach dem Aufdecken möglich.")
    @APIResponse(responseCode = "204", description = "Ergebnis wurde in GitLab übernommen")
    public Response takeToGitlab(@PathParam("planningPokerId") UUID planningPokerId, FinalizeResultRequest request) {
        planningPokerBusiness.takeToGitlab(
                planningPokerId,
                (ScrumMaster) authenticatedMemberFactory.create(),
                request.value()
        );
        return Response.noContent().build();
    }

    private Set<String> namesOf(Set<Developer> developers) {
        return developers.stream().map(Developer::getName).collect(java.util.stream.Collectors.toSet());
    }

    public record CreateSessionRequest(String scrumMasterName, long gitlabProjectId, long gitlabIssueIid) {
    }

    public record CreateSessionResponse(UUID planningPokerId) {
    }

    public record DeveloperRequest(String developerName) {
    }

    public record SelectIssueRequest(String scrumMasterName, long gitlabIssueIid) {
    }

    public record ActiveIssueResponse(long gitlabIssueIid, String title, String description) {
    }

    public record ScrumMasterRequest(String scrumMasterName) {
    }

    public record EstimateRequest(String developerName, CardValue value) {
    }

    public record FinalizeResultRequest(String scrumMasterName, CardValue value) {
    }

    public record EstimationProgressResponse(
            Set<String> estimatedDevelopers,
            Set<String> pendingDevelopers,
            List<DeveloperProgressResponse> developers
    ) {
    }

    public record DeveloperProgressResponse(String name, String joinedAt, boolean estimated) {
    }

    public record AllDevelopersEstimatedResponse(boolean allDevelopersEstimated) {
    }

    public record EstimateValueResponse(String developerName, CardValue value) {
    }

    public record NumericEstimationResponse(Integer value) {
    }
}
