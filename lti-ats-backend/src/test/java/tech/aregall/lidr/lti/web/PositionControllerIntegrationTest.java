package tech.aregall.lidr.lti.web;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import tech.aregall.lidr.lti.TestcontainersConfiguration;
import tech.aregall.lidr.lti.infrastructure.persistence.PositionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PositionControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PositionRepository positionRepository;

    private final List<UUID> createdPositionIds = new ArrayList<>();

    @BeforeAll
    void setup() {
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterEach
    void cleanup() {
        createdPositionIds.forEach(positionRepository::deleteById);
        createdPositionIds.clear();
    }

    @Test
    void getAllPositions_returnsThreeSeedPositions() {
        given()
            .when().get("/api/v1/positions")
            .then()
                .statusCode(200)
                .body("$", hasSize(3))
                .body("title", containsInAnyOrder(
                        "Backend Engineer",
                        "Product Designer",
                        "Engineering Manager"));
    }

    @Test
    void getPositionById_returnsPosition_whenExists() {
        String id = given()
            .when().get("/api/v1/positions")
            .then()
                .statusCode(200)
                .extract().path("[0].id");

        given()
            .when().get("/api/v1/positions/" + id)
            .then()
                .statusCode(200)
                .body("id", equalTo(id));
    }

    @Test
    void getPositionById_returns404_whenNotFound() {
        given()
            .when().get("/api/v1/positions/" + UUID.randomUUID())
            .then()
                .statusCode(404)
                .body("error", notNullValue());
    }

    @Test
    void createPosition_returns201_withCreatedPosition() {
        String id = given()
            .contentType(ContentType.JSON)
            .body(Map.of("title", "QA Engineer", "department", "Quality", "openDate", "2025-06-01"))
        .when().post("/api/v1/positions")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("title", equalTo("QA Engineer"))
            .body("department", equalTo("Quality"))
            .extract().path("id");

        createdPositionIds.add(UUID.fromString(id));
    }

    @Test
    void createPosition_returns400_whenBodyIsInvalid() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("title", "", "department", "", "openDate", "2025-06-01"))
        .when().post("/api/v1/positions")
        .then()
            .statusCode(400);
    }

    @Test
    void updatePosition_returns200_whenExists() {
        String id = given()
            .contentType(ContentType.JSON)
            .body(Map.of("title", "Temp Role", "department", "Engineering", "openDate", "2025-07-01"))
        .when().post("/api/v1/positions")
        .then()
            .statusCode(201)
            .extract().path("id");

        createdPositionIds.add(UUID.fromString(id));

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("title", "Updated Role", "department", "Engineering", "openDate", "2025-08-01"))
        .when().put("/api/v1/positions/" + id)
        .then()
            .statusCode(200)
            .body("title", equalTo("Updated Role"));
    }

    @Test
    void updatePosition_returns404_whenNotFound() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("title", "Any Role", "department", "Engineering", "openDate", "2025-01-01"))
        .when().put("/api/v1/positions/" + UUID.randomUUID())
        .then()
            .statusCode(404);
    }

    @Test
    void deletePosition_returns204_whenExists() {
        String id = given()
            .contentType(ContentType.JSON)
            .body(Map.of("title", "To Delete", "department", "Engineering", "openDate", "2025-09-01"))
        .when().post("/api/v1/positions")
        .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when().delete("/api/v1/positions/" + id)
            .then()
                .statusCode(204);
    }

    @Test
    void deletePosition_returns404_whenNotFound() {
        given()
            .when().delete("/api/v1/positions/" + UUID.randomUUID())
            .then()
                .statusCode(404);
    }
}
