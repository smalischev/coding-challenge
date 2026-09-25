package webservice;

import business.GitLabIssueGateway;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;

@QuarkusTest
class PlanningPokerResourceTest {
    @InjectMock
    GitLabIssueGateway gitLabIssueGateway;

    @BeforeEach
    void authenticate() {
        given().contentType(JSON).body(Map.of("username", "Mara", "password", "a-secure-test-password"))
                .when().post("/auth/register")
                .then().statusCode(201);

        String accessToken = given().contentType(JSON)
                .body(Map.of("username", "Mara", "password", "a-secure-test-password"))
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken");

        RestAssured.requestSpecification = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
    }

    // Prüft den vollständigen REST-Ablauf einer Planning-Poker-Session.
    @Test
    void planningPokerSessionCanBeManagedThroughRestEndpoints() {
        String planningPokerId = given()
                .contentType(JSON)
                .body(Map.of("scrumMasterName", "Mara", "gitlabProjectId", 123, "gitlabIssueIid", 42))
                .when().post("/planning-pokers")
                .then().statusCode(201)
                .extract().path("planningPokerId");

        given().contentType(JSON).body(Map.of("scrumMasterName", "Mara", "gitlabIssueIid", 42))
                .when().put("/planning-pokers/{id}/active-issue", planningPokerId)
                .then().statusCode(204);

        join(planningPokerId, "Alex");
        join(planningPokerId, "Kim");

        given().when().get("/planning-pokers/{id}/active-round/progress", planningPokerId)
                .then().statusCode(200)
                .body("estimatedDevelopers", equalTo(java.util.List.of()))
                .body("pendingDevelopers", containsInAnyOrder("Alex", "Kim"));

        given().contentType(JSON).body(Map.of("scrumMasterName", "Mara"))
                .when().post("/planning-pokers/{id}/active-issue/release", planningPokerId)
                .then().statusCode(204);

        estimate(planningPokerId, "Alex", "FIVE");
        estimate(planningPokerId, "Kim", "EIGHT");

        given().when().get("/planning-pokers/{id}/active-round/all-developers-estimated", planningPokerId)
                .then().statusCode(200).body("allDevelopersEstimated", equalTo(true));

        given().contentType(JSON).body(Map.of("scrumMasterName", "Mara"))
                .when().post("/planning-pokers/{id}/active-round/reveal", planningPokerId)
                .then().statusCode(204);

        given().when().get("/planning-pokers/{id}/active-round/estimates", planningPokerId)
                .then().statusCode(200).body("developerName", containsInAnyOrder("Alex", "Kim"));
        given().when().get("/planning-pokers/{id}/active-round/estimate-groups", planningPokerId)
                .then().statusCode(200).body("FIVE", equalTo(1)).body("EIGHT", equalTo(1));
        given().when().get("/planning-pokers/{id}/active-round/average", planningPokerId)
                .then().statusCode(200).body("value", equalTo(6));
        given().when().get("/planning-pokers/{id}/active-round/most-frequent-value", planningPokerId)
                .then().statusCode(200).body("value", equalTo(5));

        given().contentType(JSON).body(Map.of("scrumMasterName", "Mara", "value", "FIVE"))
                .when().post("/planning-pokers/{id}/active-round/result", planningPokerId)
                .then().statusCode(204);
        verify(gitLabIssueGateway).addScopedLabel(123L, 42L, "planning-poker::5");

        given().contentType(JSON).body(Map.of("scrumMasterName", "Mara"))
                .when().post("/planning-pokers/{id}/active-round", planningPokerId)
                .then().statusCode(204);
    }

    private void join(String planningPokerId, String developerName) {
        given().contentType(JSON).body(Map.of("developerName", developerName))
                .when().post("/planning-pokers/{id}/developers", planningPokerId)
                .then().statusCode(204);
    }

    private void estimate(String planningPokerId, String developerName, String value) {
        given().contentType(JSON).body(Map.of("developerName", developerName, "value", value))
                .when().post("/planning-pokers/{id}/active-round/estimates", planningPokerId)
                .then().statusCode(204);
    }
}
