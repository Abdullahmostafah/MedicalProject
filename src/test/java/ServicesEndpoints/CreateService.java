package ServicesEndpoints;

import Base.TestBase;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.*;

@Epic("Service")
@Feature("Create Service")
@DisplayName("Create Service API")
public class CreateService extends TestBase {

    public void create_service(){
      Response response=
              given()
                .spec(requestSpecs)
                .config(config)
                .body("")
                .when()
                .post("/Service/Insert")
                .then()
                .statusCode(200)
                .extract().response();

    }


}
