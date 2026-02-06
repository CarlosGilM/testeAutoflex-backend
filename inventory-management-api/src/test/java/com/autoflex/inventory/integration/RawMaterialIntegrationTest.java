package com.autoflex.inventory.integration;

import com.autoflex.inventory.dto.request.RawMaterialRequestDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class RawMaterialIntegrationTest {

    @Test
    void testFullRawMaterialLifecycle() {
        RawMaterialRequestDTO newMaterial = new RawMaterialRequestDTO("Plástico ABS", 500.0);

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(newMaterial)
                .when()
                .post("/api/raw-materials")
                .then()
                .statusCode(201)
                .body("name", is("Plástico ABS"))
                .extract().path("code");

        given()
                .when()
                .get("/api/raw-materials/" + id)
                .then()
                .statusCode(200)
                .body("stockQuantity", is(500.0f));


        RawMaterialRequestDTO updateMaterial = new RawMaterialRequestDTO("Plástico ABS Reforçado", 450.0);

        given()
                .contentType(ContentType.JSON)
                .body(updateMaterial)
                .when()
                .put("/api/raw-materials/" + id)
                .then()
                .statusCode(200)
                .body("name", is("Plástico ABS Reforçado"));

        given()
                .when()
                .delete("/api/raw-materials/" + id)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/api/raw-materials/" + id)
                .then()
                .statusCode(404);
    }
}