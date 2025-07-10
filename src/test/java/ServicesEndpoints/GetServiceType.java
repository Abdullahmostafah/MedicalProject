package ServicesEndpoints;

import Base.TestBase;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;

@Epic("Service")
@Feature("View Service Types")
@DisplayName("Get Service Types API")
public class GetServiceType extends TestBase {

    public void get_service_type(){
        Response response=
                given()
                        .spec(requestSpecs)
                        .config(config)
                        .body("")
                        .when()
                        .get("/Service/Type")
                        .then()
                        .statusCode(200)
                        .extract().response();

    }

}
