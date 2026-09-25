package webservice;

import business.PlanningPokerBusiness;
import domain.CardValue;
import domain.Developer;
import domain.EstimationProgress;
import domain.Issue;
import domain.ScrumMaster;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Path("/planning-pokers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlanningPokerResource {
    @Inject
    PlanningPokerBusiness planningPokerBusiness;

    @POST
    public Response createSession(CreateSessionRequest request) {
        UUID planningPokerId = planningPokerBusiness.createPlanningPoker(
                new ScrumMaster(request.scrumMasterName()),
                request.gitlabProjectId(),
                new Issue(request.gitlabIssueIid())
        );

        return Response.status(Response.Status.CREATED)
                .entity(new CreateSessionResponse(planningPokerId))
                .build();
    }

    @POST
    @Path("/{planningPokerId}/developers")
    public Response join(@PathParam("planningPokerId") UUID planningPokerId, DeveloperRequest request) {
        Developer developer = new Developer(request.developerName());
        planningPokerBusiness.join(planningPokerId, developer, developer);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{planningPokerId}/active-issue")
    public Response selectIssue(@PathParam("planningPokerId") UUID planningPokerId, SelectIssueRequest request) {
        planningPokerBusiness.selectIssue(
                planningPokerId,
                new ScrumMaster(request.scrumMasterName()),
                new Issue(request.gitlabIssueIid())
        );
        return Response.noContent().build();
    }

    @POST
    @Path("/{planningPokerId}/active-issue/release")
    public Response releaseActiveIssue(@PathParam("planningPokerId") UUID planningPokerId, ScrumMasterRequest request) {
        planningPokerBusiness.releaseActiveIssue(planningPokerId, new ScrumMaster(request.scrumMasterName()));
        return Response.noContent().build();
    }

    @POST
    @Path("/{planningPokerId}/active-round/estimates")
    public Response estimate(@PathParam("planningPokerId") UUID planningPokerId, EstimateRequest request) {
        Developer developer = new Developer(request.developerName());
        planningPokerBusiness.estimate(planningPokerId, developer, developer, request.value());
        return Response.noContent().build();
    }

    @GET
    @Path("/{planningPokerId}/active-round/progress")
    public EstimationProgressResponse getEstimationProgress(@PathParam("planningPokerId") UUID planningPokerId) {
        EstimationProgress progress = planningPokerBusiness.getEstimationProgress(planningPokerId);
        return new EstimationProgressResponse(
                namesOf(progress.estimatedDevelopers()),
                namesOf(progress.pendingDevelopers())
        );
    }

    @GET
    @Path("/{planningPokerId}/active-round/all-developers-estimated")
    public AllDevelopersEstimatedResponse allDevelopersEstimated(@PathParam("planningPokerId") UUID planningPokerId) {
        return new AllDevelopersEstimatedResponse(planningPokerBusiness.allDevelopersEstimated(planningPokerId));
    }

    @POST
    @Path("/{planningPokerId}/active-round/reveal")
    public Response reveal(@PathParam("planningPokerId") UUID planningPokerId, ScrumMasterRequest request) {
        ScrumMaster scrumMaster = new ScrumMaster(request.scrumMasterName());
        planningPokerBusiness.reveal(planningPokerId, scrumMaster, scrumMaster);
        return Response.noContent().build();
    }

    @GET
    @Path("/{planningPokerId}/active-round/estimates")
    public List<EstimateValueResponse> getEstimateValues(@PathParam("planningPokerId") UUID planningPokerId) {
        return planningPokerBusiness.getEstimateValues(planningPokerId).entrySet().stream()
                .map(entry -> new EstimateValueResponse(entry.getKey().getName(), entry.getValue()))
                .toList();
    }

    @GET
    @Path("/{planningPokerId}/active-round/estimate-groups")
    public Map<CardValue, Long> groupEstimates(@PathParam("planningPokerId") UUID planningPokerId) {
        return planningPokerBusiness.groupEstimates(planningPokerId);
    }

    @GET
    @Path("/{planningPokerId}/active-round/average")
    public NumericEstimationResponse calculateAverage(@PathParam("planningPokerId") UUID planningPokerId) {
        return new NumericEstimationResponse(
                planningPokerBusiness.calculateAverage(planningPokerId).stream().boxed().findFirst().orElse(null)
        );
    }

    @GET
    @Path("/{planningPokerId}/active-round/most-frequent-value")
    public NumericEstimationResponse findMostFrequentValue(@PathParam("planningPokerId") UUID planningPokerId) {
        return new NumericEstimationResponse(
                planningPokerBusiness.findMostFrequentValue(planningPokerId).stream().boxed().findFirst().orElse(null)
        );
    }

    @POST
    @Path("/{planningPokerId}/active-round")
    public Response startNewRound(@PathParam("planningPokerId") UUID planningPokerId, ScrumMasterRequest request) {
        planningPokerBusiness.startNewRound(planningPokerId, new ScrumMaster(request.scrumMasterName()));
        return Response.noContent().build();
    }

    @POST
    @Path("/{planningPokerId}/active-round/result")
    public Response takeToGitlab(@PathParam("planningPokerId") UUID planningPokerId, FinalizeResultRequest request) {
        planningPokerBusiness.takeToGitlab(
                planningPokerId,
                new ScrumMaster(request.scrumMasterName()),
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

    public record ScrumMasterRequest(String scrumMasterName) {
    }

    public record EstimateRequest(String developerName, CardValue value) {
    }

    public record FinalizeResultRequest(String scrumMasterName, CardValue value) {
    }

    public record EstimationProgressResponse(Set<String> estimatedDevelopers, Set<String> pendingDevelopers) {
    }

    public record AllDevelopersEstimatedResponse(boolean allDevelopersEstimated) {
    }

    public record EstimateValueResponse(String developerName, CardValue value) {
    }

    public record NumericEstimationResponse(Integer value) {
    }
}
