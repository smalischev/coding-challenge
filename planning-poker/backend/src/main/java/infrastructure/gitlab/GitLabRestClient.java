package infrastructure.gitlab;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v4")
@RegisterRestClient(configKey = "gitlab-api")
public interface GitLabRestClient {
    @GET
    @Path("/projects/{projectId}/issues/{issueIid}")
    GitLabIssueResponse getIssue(
            @HeaderParam("PRIVATE-TOKEN") String accessToken,
            @PathParam("projectId") long projectId,
            @PathParam("issueIid") long issueIid
    );

    @PUT
    @Path("/projects/{projectId}/issues/{issueIid}")
    void addLabel(
            @HeaderParam("PRIVATE-TOKEN") String accessToken,
            @PathParam("projectId") long projectId,
            @PathParam("issueIid") long issueIid,
            @QueryParam("add_labels") String label
    );

    record GitLabIssueResponse(long iid, String title, String description) {
    }
}
