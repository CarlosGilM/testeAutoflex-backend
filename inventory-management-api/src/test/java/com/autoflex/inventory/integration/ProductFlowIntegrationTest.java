package com.autoflex.inventory.integration;

import com.autoflex.inventory.dto.request.ProductCompositionRequestDTO;
import com.autoflex.inventory.dto.request.ProductRequestDTO;
import com.autoflex.inventory.dto.request.RawMaterialRequestDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ProductFlowIntegrationTest {

    @Test
    void testProductionFlowAndConstraints() {
        RawMaterialRequestDTO material = new RawMaterialRequestDTO("Couro Sintético", 100.0);

        Integer materialId = given()
                .contentType(ContentType.JSON)
                .body(material)
                .when().post("/api/raw-materials")
                .then().statusCode(201)
                .extract().path("code");

        ProductCompositionRequestDTO comp = new ProductCompositionRequestDTO(null, materialId.longValue(), 2.0);
        ProductRequestDTO product = new ProductRequestDTO("Poltrona", BigDecimal.valueOf(350.00), List.of(comp));

        Integer productId = given()
                .contentType(ContentType.JSON)
                .body(product)
                .when().post("/api/products")
                .then().statusCode(201)
                .extract().path("code");

        given()
                .when().get("/api/products/production-suggestion")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("find { it.productCode == " + productId + " }.quantityToProduce", is(50));

        given()
                .when().delete("/api/raw-materials/" + materialId)
                .then()
                .statusCode(409);

        given().when().delete("/api/products/" + productId).then().statusCode(204);

        given().when().delete("/api/raw-materials/" + materialId).then().statusCode(204);
    }
}