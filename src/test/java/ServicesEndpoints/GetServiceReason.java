package ServicesEndpoints;

import Base.TestBase;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;

@Epic("Service")
@Feature("View Service Reasons")
@DisplayName("Get Service Reasons API")
public class GetServiceReason extends TestBase {

    public void get_service_reason(){
        Response response=
                given()
                        .spec(requestSpecs)
                        .config(config)
                        .body("")
                        .when()
                        .get("/Service/Reasons")
                        .then()
                        .statusCode(200)
                        .extract().response();

    }

}


