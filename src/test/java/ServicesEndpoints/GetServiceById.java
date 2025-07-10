package ServicesEndpoints;

import Base.TestBase;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;

@Epic("Service")
@Feature("View Service Details")
@DisplayName("Get Service by ID API")
public class GetServiceById extends TestBase {

    public void get_service_by_id(){
        Response response=
                given()
                        .spec(requestSpecs)
                        .config(config)
                        .body("")
                        .when()
                        .get("/Service/GetById")
                        .then()
                        .statusCode(200)
                        .extract().response();

    }


}
