package APIs_TCs.Service_TCs;

import Base.TestBase;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

@Epic("Service Management")
@Story("1.2")
@Feature("List Services")
@Listeners({io.qameta.allure.testng.AllureTestNg.class})
public class ListService extends TestBase {

    @Test(priority = 1, description = "This API retrieves all services created")
    @Severity(SeverityLevel.CRITICAL)
    public void list_service() {
        Response response =
                given()
                        .spec(requestSpecs)
                        .config(config)
                        .body("{}")
                        .when()
                        .post("/Service/Get")
                        .then()
                        .statusCode(400)
                        .extract().response();

        // Add response assertions if required
    }
}
