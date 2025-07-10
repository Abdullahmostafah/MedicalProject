package ServicesEndpoints;

import Base.TestBase;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;


@Epic("Service")
@Feature("View Service Custom Properties")
@DisplayName("Get Service Custom Properties API")
public class GetServiceCustomProperties extends TestBase {

    public void get_service_custom_properties(){
        Response response=
                given()
                        .spec(requestSpecs)
                        .config(config)
                        .body("")
                        .when()
                        .get("/Service/CustomProperties")
                        .then()
                        .statusCode(200)
                        .extract().response();

    }

}
