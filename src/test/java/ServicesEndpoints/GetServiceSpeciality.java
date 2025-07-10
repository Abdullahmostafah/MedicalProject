package ServicesEndpoints;

import Base.TestBase;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;

@Epic("Service")
@Feature("View Service Speciality")
@DisplayName("Get Service Speciality API")
public class GetServiceSpeciality extends TestBase {

    public void get_service_speciality(){

        Response response=
                given()
                        .spec(requestSpecs)
                        .config(config)
                        .body("")
                        .when()
                        .get("/Service/Specialty")
                        .then()
                        .statusCode(200)
                        .extract().response();

    }

}

