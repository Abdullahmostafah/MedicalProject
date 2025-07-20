package Tests.Service_TCs.Create;

import Base.TestBase;
import Base.ValidationUtils;
import Data.ServiceData;
import Data.Common.TestData;
import Base.TestDataLoader;
import Utils.ConfigReaderWriter;
import Validators.ValidationErrorConsts;
import com.fasterxml.jackson.core.type.TypeReference;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static Base.ValidationUtils.assertValidationContains;
import static io.restassured.RestAssured.given;

@Owner("Abdullah")
@Epic("Service Management")
@Story("1.1")
@Feature("Create Service")
@Listeners({io.qameta.allure.testng.AllureTestNg.class})
public class CreateService extends TestBase {


    private static final String BASE_PATH = "src/test/resources/TestData/Service_TD/";
    private static final String FILE_NAME = "EnhancedServiceTestData.json";
    private ValidationUtils validationUtils = new ValidationUtils();
    //  private static String SERVICE_ID = ConfigReaderWriter.getPropKey("testdata.createdServiceId");


    @Test(priority = 1, description = "Create service with valid mandatory fields")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify service can be created with only mandatory fields")
    public void createServiceWithMandatoryFields() throws Exception {
        TestData<ServiceData> testData = loadTestData("01.Check adding a service with only mandatory attributes only");

        Response response = given()
                .spec(requestSpecification)
                .body(testData.getData())
                .when()
                .post("/Service/Insert");

        assertPositiveResponse(response, ServiceData.class);
        verifyResponseMatchesRequest(testData.getData(), response);
    }

    @Test(priority = 2, description = "Create service with all fields including future effective date")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify service can be created with all possible fields including future effective date")
    public void createServiceWithAllFields() throws Exception {
        TestData<ServiceData> testData = loadTestData("02.Check adding a service with mandatory and optional attributes and effective data in future");

        Response response = given()
                .spec(requestSpecification)
                .body(testData.getData())
                .when()
                .post("/Service/Insert");

        assertPositiveResponse(response, ServiceData.class);
        verifyResponseMatchesRequest(testData.getData(), response);

        String extractedServiceId = response.jsonPath().getString("data.row.id");
        ConfigReaderWriter.saveTestData("createdServiceId", extractedServiceId);
        System.out.println("Saved service ID: " + extractedServiceId);
    }

    @Test(priority = 8, description = "Prevent duplicate service attributes")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify system prevents creation of service with duplicate attributes")
    public void preventDuplicateServiceAttributes() throws Exception {
//        // First create valid service
//        TestData<ServiceData> validData = loadTestData("02.Check adding a service with mandatory and optional attributes and effective data in future");
//        given().spec(requestSpecification).body(validData.getData()).post("/Service/Insert");

        // Then attempt duplicate
        TestData<ServiceData> duplicateData = loadTestData("03.Check sending request with a used fields before");
        Response response = given()
                .spec(requestSpecification)
                .body(duplicateData.getData())
                .when()
                .post("/Service/Insert");

        assertNegativeResponse(response,
                ValidationErrorConsts.Columns.CODE.getColumn(),
                "already exists");
    }

    @Test(priority = 4, description = "Missing mandatory fields validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify system rejects request when mandatory fields are missing")
    public void validateMissingMandatoryFields() throws Exception {
        TestData<ServiceData> testData = loadTestData("04. Missing mandatory fields (negative)");

        Response response = given()
                .spec(requestSpecification)
                .body(testData.getData())
                .when()
                .post("/Service/Insert");

        assertNegativeResponse(response);

        // Verify all mandatory fields are reported as required
        assertValidationContains(response, ValidationErrorConsts.Columns.CODE.getColumn(), "required");
        assertValidationContains(response, ValidationErrorConsts.Columns.NAME_EN.getColumn(), "required");
        assertValidationContains(response, ValidationErrorConsts.Columns.TITLE_EN.getColumn(), "required");
        assertValidationContains(response, ValidationErrorConsts.Columns.TITLE_AR.getColumn(), "required");
    }

    @Test(priority = 5, description = "Field length validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify system rejects fields exceeding maximum length")
    public void validateFieldLengths() throws Exception {
        TestData<ServiceData> testData = loadTestData("05.Check exceeding max length for fields");

        Response response = given()
                .spec(requestSpecification)
                .body(testData.getData())
                .when()
                .post("/Service/Insert");

        assertNegativeResponse(response);

        // Verify length violations for all fields
        assertValidationContains(response, ValidationErrorConsts.Columns.CODE.getColumn(), "length");
        assertValidationContains(response, ValidationErrorConsts.Columns.NAME_EN.getColumn(), "length");
        // assertValidationContains(response, ValidationErrorConsts.Columns.NAME_AR.getColumn(), "length");
        assertValidationContains(response, ValidationErrorConsts.Columns.TITLE_EN.getColumn(), "length");
        //assertValidationContains(response, ValidationErrorConsts.Columns.TITLE_AR.getColumn(), "length");
        assertValidationContains(response, ValidationErrorConsts.Columns.ABBREVIATION_EN.getColumn(), "length");
        assertValidationContains(response, ValidationErrorConsts.Columns.ABBREVIATION_AR.getColumn(), "length");
    }

    @Test(priority = 6, description = "Invalid data format validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify system rejects invalid cost, date format, and past effective date")
    public void validateInvalidDataFormats() throws Exception {
        TestData<ServiceData> testData = loadTestData("06.Check sending invalid reference cost (negative), invalid date format, custom property value, and effective date in the past.");

        Response response = given()
                .spec(requestSpecification)
                .body(testData.getData())
                .when()
                .post("/Service/Insert");


        assertNegativeResponse(response);

        // Use both apostrophe types in validation
        assertValidationContains(
                response,
                ValidationErrorConsts.Columns.REFERENCE_AVERAGE_COST.getColumn(),
                "can't be negative",  // straight apostrophe
                "can’t be negative"   // curly apostrophe
        );
    }

    @Test(priority = 7, description = "Invalid reference IDs validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify system rejects invalid custom property, specialty, and group IDs")
    public void validateInvalidReferenceIds() throws Exception {
        TestData<ServiceData> testData = loadTestData("07.Check sending invalid custom property value, specialty, Group");

        Response response = given()
                .spec(requestSpecification)
                .body(testData.getData())
                .when()
                .post("/Service/Insert");

        assertNegativeResponse(response);

        // Verify invalid reference checks
        assertValidationContains(response, ValidationErrorConsts.Columns.GROUP.getColumn(), "Selected Group is not valid");
        assertValidationContains(response, ValidationErrorConsts.Columns.SPECIALTY.getColumn(), "Selected Specialty is not valid");
        assertValidationContains(response, ValidationErrorConsts.Columns.CUSTOM_PROPERTY_VALUES.getColumn(), "Selected Custom Property Values is not valid");
    }

    @Test(priority = 3, description = "Soft deleted attributes validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify system rejects soft deleted reference attributes")
    public void validateSoftDeletedAttributes() throws Exception {
        // Get saved ID from test #2
        String serviceId = ConfigReaderWriter.getTestData("createdServiceId");
        System.out.println(serviceId);
        Assert.assertNotNull(serviceId, "Service ID not found. Run test #2 first.");

        // Rest of your test implementation...
        Response deleteResponse = given()
                .spec(requestSpecification)
                .queryParam("id", serviceId)
                .when()
                .delete("/Service/Delete")
                .then()
                .statusCode(200)
                .extract()
                .response();
        System.out.println("Soft-deleted service ID: " + serviceId);


        // Load test data for soft-deleted reference check
        TestData<ServiceData> testData = loadTestData("09.Check sending request with soft deleted attributes");

        // Inject soft-deleted reference ID into test data (example: groupId)
        ServiceData requestData = testData.getData();
        requestData.setGroupId(serviceId); // if groupId is meant to refer to deleted service

        // Send request and validate
        Response response = given()
                .spec(requestSpecification)
                .body(requestData)
                .when()
                .post("/Service/Insert")
                .then()
                .extract()
                .response();

        assertNegativeResponse(response);
        assertValidationContains(response, ValidationErrorConsts.Columns.CODE.getColumn(), "deleted");
    }

    private TestData<ServiceData> loadTestData(String testCaseName) throws Exception {
        return TestDataLoader.loadTestCaseByName(
                BASE_PATH,
                FILE_NAME,
                testCaseName,
                new TypeReference<List<TestData<ServiceData>>>() {
                });
    }

    private void verifyResponseMatchesRequest(ServiceData requestData, Response response) {
        // Verify all fields that were sent in request exist in response with same values
        ValidationUtils.assertFieldValuesMatchRequest(response, requestData);

        // Additional service-specific validations
        if (requestData.getCustomPropertyValues() != null) {
            ValidationUtils.assertCustomPropertiesMatch(response, requestData.getCustomPropertyValues());
        }
    }
}