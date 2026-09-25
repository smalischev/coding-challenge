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
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/planning-pokers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlanningPokerResource {
    @Inject
    PlanningPokerBusiness planningPokerBusiness;

    @POST
    public Response createSession(CreateSessionRequest request) {
        planningPokerBusiness.createPlanningPoker(
                new ScrumMaster(request.scrumMasterName()),
                request.gitlabProjectId(),
                new Issue(request.gitlabIssueIid())
        );

        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/developers")
    public Response join(DeveloperRequest request) {
        Developer developer = new Developer(request.developerName());
        planningPokerBusiness.join(developer, developer);
        return Response.noContent().build();
    }

    @PUT
    @Path("/active-issue")
    public Response selectIssue(SelectIssueRequest request) {
        planningPokerBusiness.selectIssue(
                new ScrumMaster(request.scrumMasterName()),
                new Issue(request.gitlabIssueIid())
        );
        return Response.noContent().build();
    }

    @POST
    @Path("/active-issue/release")
    public Response releaseActiveIssue(ScrumMasterRequest request) {
        planningPokerBusiness.releaseActiveIssue(new ScrumMaster(request.scrumMasterName()));
        return Response.noContent().build();
    }

    @POST
    @Path("/active-round/estimates")
    public Response estimate(EstimateRequest request) {
        Developer developer = new Developer(request.developerName());
        planningPokerBusiness.estimate(developer, developer, request.value());
        return Response.noContent().build();
    }

    @GET
    @Path("/active-round/progress")
    public EstimationProgressResponse getEstimationProgress() {
        EstimationProgress progress = planningPokerBusiness.getEstimationProgress();
        return new EstimationProgressResponse(
                namesOf(progress.estimatedDevelopers()),
                namesOf(progress.pendingDevelopers())
        );
    }

    @GET
    @Path("/active-round/all-developers-estimated")
    public AllDevelopersEstimatedResponse allDevelopersEstimated() {
        return new AllDevelopersEstimatedResponse(planningPokerBusiness.allDevelopersEstimated());
    }

    @POST
    @Path("/active-round/reveal")
    public Response reveal(ScrumMasterRequest request) {
        ScrumMaster scrumMaster = new ScrumMaster(request.scrumMasterName());
        planningPokerBusiness.reveal(scrumMaster, scrumMaster);
        return Response.noContent().build();
    }

    @GET
    @Path("/active-round/estimates")
    public List<EstimateValueResponse> getEstimateValues() {
        return planningPokerBusiness.getEstimateValues().entrySet().stream()
                .map(entry -> new EstimateValueResponse(entry.getKey().getName(), entry.getValue()))
                .toList();
    }

    @GET
    @Path("/active-round/estimate-groups")
    public Map<CardValue, Long> groupEstimates() {
        return planningPokerBusiness.groupEstimates();
    }

    @GET
    @Path("/active-round/average")
    public NumericEstimationResponse calculateAverage() {
        return new NumericEstimationResponse(
                planningPokerBusiness.calculateAverage().stream().boxed().findFirst().orElse(null)
        );
    }

    @GET
    @Path("/active-round/most-frequent-value")
    public NumericEstimationResponse findMostFrequentValue() {
        return new NumericEstimationResponse(
                planningPokerBusiness.findMostFrequentValue().stream().boxed().findFirst().orElse(null)
        );
    }

    @POST
    @Path("/active-round")
    public Response startNewRound(ScrumMasterRequest request) {
        planningPokerBusiness.startNewRound(new ScrumMaster(request.scrumMasterName()));
        return Response.noContent().build();
    }

    @POST
    @Path("/active-round/result")
    public Response takeToGitlab(FinalizeResultRequest request) {
        planningPokerBusiness.takeToGitlab(
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
