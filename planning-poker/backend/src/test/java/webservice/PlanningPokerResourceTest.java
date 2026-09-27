package webservice;

import business.GitLabIssueGateway;
import domain.Issue;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class PlanningPokerResourceTest {
    @InjectMock
    GitLabIssueGateway gitLabIssueGateway;
    private String scrumMasterToken;
    private String alexToken;
    private String kimToken;
    private String otherScrumMasterToken;
    private String scrumMasterName;
    private String alexName;
    private String kimName;
    private String otherScrumMasterName;

    @BeforeEach
    void authenticate() {
        RestAssured.requestSpecification = null;
        String testRun = UUID.randomUUID().toString();
        scrumMasterName = "Mara-" + testRun;
        alexName = "Alex-" + testRun;
        kimName = "Kim-" + testRun;
        otherScrumMasterName = "Nina-" + testRun;
        when(gitLabIssueGateway.getIssue(anyLong(), anyLong())).thenAnswer(invocation -> {
            long issueIid = invocation.getArgument(1);
            return new Issue(issueIid, "Issue " + issueIid, "Issue description " + issueIid);
        });
        scrumMasterToken = registerAndLogin(scrumMasterName, "SCRUM_MASTER");
        alexToken = registerAndLogin(alexName, "DEVELOPER");
        kimToken = registerAndLogin(kimName, "DEVELOPER");
        otherScrumMasterToken = registerAndLogin(otherScrumMasterName, "SCRUM_MASTER");
    }

    // Prüft den vollständigen REST-Ablauf einer Planning-Poker-Session.
    @Test
    void planningPokerSessionCanBeManagedThroughRestEndpoints() {
        String planningPokerId = as(scrumMasterToken)
                .contentType(JSON)
                .body(Map.of("scrumMasterName", scrumMasterName, "gitlabProjectId", 123, "gitlabIssueIid", 42))
                .when().post("/planning-pokers")
                .then().statusCode(201)
                .extract().path("planningPokerId");

        as(scrumMasterToken).when().get("/planning-pokers/{id}/active-issue", planningPokerId)
                .then().statusCode(200)
                .body("gitlabIssueIid", equalTo(42))
                .body("title", equalTo("Issue 42"))
                .body("description", equalTo("Issue description 42"));

        as(scrumMasterToken).contentType(JSON).body(Map.of("scrumMasterName", scrumMasterName, "gitlabIssueIid", 42))
                .when().put("/planning-pokers/{id}/active-issue", planningPokerId)
                .then().statusCode(204);

        join(planningPokerId, alexName, alexToken);
        join(planningPokerId, kimName, kimToken);

        as(scrumMasterToken).when().get("/planning-pokers/{id}/active-round/progress", planningPokerId)
                .then().statusCode(200)
                .body("estimatedDevelopers", equalTo(java.util.List.of()))
                .body("pendingDevelopers", containsInAnyOrder(alexName, kimName))
                .body("developers.name", equalTo(java.util.List.of(alexName, kimName)))
                .body("developers.joinedAt", everyItem(notNullValue()));

        as(scrumMasterToken).contentType(JSON).body(Map.of("scrumMasterName", scrumMasterName))
                .when().post("/planning-pokers/{id}/active-issue/release", planningPokerId)
                .then().statusCode(204);

        estimate(planningPokerId, alexName, "FIVE", alexToken);
        estimate(planningPokerId, kimName, "EIGHT", kimToken);

        as(scrumMasterToken).when().get("/planning-pokers/{id}/active-round/all-developers-estimated", planningPokerId)
                .then().statusCode(200).body("allDevelopersEstimated", equalTo(true));

        as(scrumMasterToken).contentType(JSON).body(Map.of("scrumMasterName", scrumMasterName))
                .when().post("/planning-pokers/{id}/active-round/reveal", planningPokerId)
                .then().statusCode(204);

        as(scrumMasterToken).when().get("/planning-pokers/{id}/active-round/estimates", planningPokerId)
                .then().statusCode(200).body("developerName", containsInAnyOrder(alexName, kimName));
        as(scrumMasterToken).when().get("/planning-pokers/{id}/active-round/estimate-groups", planningPokerId)
                .then().statusCode(200).body("FIVE", equalTo(1)).body("EIGHT", equalTo(1));
        as(scrumMasterToken).when().get("/planning-pokers/{id}/active-round/average", planningPokerId)
                .then().statusCode(200).body("value", equalTo(6));
        as(scrumMasterToken).when().get("/planning-pokers/{id}/active-round/most-frequent-value", planningPokerId)
                .then().statusCode(200).body("value", equalTo(5));

        as(scrumMasterToken).contentType(JSON).body(Map.of("scrumMasterName", scrumMasterName, "value", "FIVE"))
                .when().post("/planning-pokers/{id}/active-round/result", planningPokerId)
                .then().statusCode(204);
        verify(gitLabIssueGateway).addScopedLabel(123L, 42L, "planning-poker::5");

        as(scrumMasterToken).contentType(JSON).body(Map.of("scrumMasterName", scrumMasterName))
                .when().post("/planning-pokers/{id}/active-round", planningPokerId)
                .then().statusCode(204);
    }

    // Prüft, dass ein Developer keine Planning-Poker-Session erstellen darf, weil dies dem Scrum Master vorbehalten ist.
    @Test
    void developerCannotCreatePlanningPokerSession() {
        as(alexToken)
                .contentType(JSON)
                .body(Map.of("gitlabProjectId", 123, "gitlabIssueIid", 42))
                .when().post("/planning-pokers")
                .then().statusCode(403);
    }

    // Prüft, dass ein Scrum Master den Ereignis-Stream einer fremden Session nicht abonnieren darf.
    @Test
    void nonOwnerCannotSubscribeToEstimationEvents() {
        String planningPokerId = as(scrumMasterToken)
                .contentType(JSON)
                .body(Map.of("gitlabProjectId", 123, "gitlabIssueIid", 42))
                .when().post("/planning-pokers")
                .then().statusCode(201)
                .extract().path("planningPokerId");

        as(otherScrumMasterToken)
                .when().get("/planning-pokers/{id}/active-round/events", planningPokerId)
                .then().statusCode(403);
    }

    // Prüft, dass der Session-Owner nach der letzten Schätzung über den SSE-Stream benachrichtigt wird.
    @Test
    void scrumMasterReceivesSseNotificationWhenAllDevelopersEstimated() throws Exception {
        String planningPokerId = as(scrumMasterToken)
                .contentType(JSON)
                .body(Map.of("gitlabProjectId", 123, "gitlabIssueIid", 42))
                .when().post("/planning-pokers")
                .then().statusCode(201)
                .extract().path("planningPokerId");
        join(planningPokerId, alexName, alexToken);
        join(planningPokerId, kimName, kimToken);
        as(scrumMasterToken)
                .contentType(JSON)
                .when().post("/planning-pokers/{id}/active-issue/release", planningPokerId)
                .then().statusCode(204);

        HttpRequest request = HttpRequest.newBuilder(URI.create(
                        "http://localhost:" + RestAssured.port + "/planning-pokers/" + planningPokerId + "/active-round/events"))
                .header("Accept", "text/event-stream")
                .header("Authorization", "Bearer " + scrumMasterToken)
                .GET()
                .build();
        HttpResponse<InputStream> sseResponse = HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .get(5, TimeUnit.SECONDS);
        assertEquals(200, sseResponse.statusCode());

        estimate(planningPokerId, alexName, "FIVE", alexToken);
        estimate(planningPokerId, kimName, "EIGHT", kimToken);

        try (InputStream stream = sseResponse.body();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String eventName = null;
            String eventData = null;
            String line;
            while ((line = reader.readLine()) != null && eventData == null) {
                if (line.startsWith("event:")) {
                    eventName = line.substring("event:".length()).trim();
                }
                if (line.startsWith("data:")) {
                    eventData = line.substring("data:".length()).trim();
                }
            }

            assertEquals("all-developers-estimated", eventName);
            assertEquals("{\"allDevelopersEstimated\":true}", eventData);
        }
    }

    private String registerAndLogin(String username, String role) {
        given().contentType(JSON).body(Map.of("username", username, "password", "a-secure-test-password", "role", role))
                .when().post("/auth/register")
                .then().statusCode(201);

        return given().contentType(JSON).body(Map.of("username", username, "password", "a-secure-test-password"))
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken");
    }

    private RequestSpecification as(String token) {
        return given().spec(new RequestSpecBuilder().addHeader("Authorization", "Bearer " + token).build());
    }

    private void join(String planningPokerId, String developerName, String token) {
        as(token).contentType(JSON).body(Map.of("developerName", developerName))
                .when().post("/planning-pokers/{id}/developers", planningPokerId)
                .then().statusCode(204);
    }

    private void estimate(String planningPokerId, String developerName, String value, String token) {
        as(token).contentType(JSON).body(Map.of("developerName", developerName, "value", value))
                .when().post("/planning-pokers/{id}/active-round/estimates", planningPokerId)
                .then().statusCode(204);
    }
}
