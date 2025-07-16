package APIs_TCs.Service_TCs;

import Base.TestBase;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

@Epic("Service Management")
@Story("1.3")
@Feature("View Service Details")
@Listeners({io.qameta.allure.testng.AllureTestNg.class})
public class GetServiceById extends TestBase {

    @Test(description = "Get service by ID returns 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @Description("This test retrieves service details by ID")
    public void get_service_by_id() {
        Response response =
                given()
                        .spec(requestSpecs)
                        .config(config)
                        .body("") // You may replace this with actual payload if needed
                        .when()
                        .get("/Service/GetById")
                        .then()
                        .statusCode(200)
                        .extract().response();

        // Add response validation/assertions if required
    }
}
