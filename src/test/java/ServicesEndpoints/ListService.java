package ServicesEndpoints;

import Base.TestBase;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Crucial for sharing state between tests

@Epic("Service")
@Feature("List/Filter/Sort Services")
@DisplayName("List All Services API")

public class ListService extends TestBase {

    @Test @Order(1)
    @Description("This Api retrieve all services created")
    public void list_service(){
        Response response=
                given()
                        .spec(requestSpecs)
                        .config(config)
                        .body("{}")
                        .when()
                        .post("/Service/Get")
                        .then()
                        .statusCode(400)
                        .extract().response();

    }

}


