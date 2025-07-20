package Base;

import Utils.*;
import Validators.ValidationError;
import Validators.ValidationResponse;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.restassured.RestAssured.*;
import static io.restassured.config.SSLConfig.sslConfig;
import static org.hamcrest.Matchers.equalTo;

/**
 * Base test class providing common setup/teardown functionality for all API tests.
 * Includes REST Assured config, Allure reporting, DB connection, logging, and report emailing.
 */
public abstract class TestBase {

    // Configuration constants
    private static final int HTTP_TIMEOUT_MS = 30000;
    private static final DateTimeFormatter LOG_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");

    // REST Assured & Logging
    protected static RestAssuredConfig restAssuredConfig;
    protected RequestSpecification requestSpecification;
    private final Map<String, PrintStream> testLogStreams = new HashMap<>();

    /* ------------------- Test Lifecycle Methods ------------------- */

    @BeforeClass
    public void globalSetup() throws IOException {
        validateTestInfrastructure();
        configureRestAssured();
        initializeReportDirectories();
    }

    @BeforeMethod
    public void setupPerTest(Method testMethod) {
        String testName = testMethod.getName();
        initializeTestLogging(testName);
        configureRequestSpecification(testName);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownAll() throws SQLException {
        try {
            generateTestReports();
        } finally {
            cleanupTestResources();
        }
        DBConnection.executeUpdate("DELETE ServiceCustomPropertiesLinkage");
        DBConnection.executeUpdate("DELETE Services");
    }

    /* ------------------- Configuration ------------------- */

    private void validateTestInfrastructure() {
        validateDatabaseConnection();
        validateBackendService();
    }

    private void validateDatabaseConnection() {
        try {
            DBConnection.getConnection();
            System.out.println("Database connection established successfully");
        } catch (Exception e) {
            throw new TestInfrastructureException("Database connection failed", e);
        }
    }

    private void validateBackendService() {
        try (Socket socket = new Socket("localhost", 5251)) {
            System.out.println("Backend service is running on port 5251");
        } catch (IOException e) {
            throw new TestInfrastructureException("Backend service not available", e);
        }
    }

    private void configureRestAssured() {
        baseURI = ConfigReaderWriter.getPropKey("base.uri");

        restAssuredConfig = config()
                .sslConfig(sslConfig().relaxedHTTPSValidation())
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", HTTP_TIMEOUT_MS)
                        .setParam("http.socket.timeout", HTTP_TIMEOUT_MS));

        enableLoggingOfRequestAndResponseIfValidationFails();
    }

    private void initializeReportDirectories() throws IOException {
        FileUtils.cleanDirectory(FileUtils.LOG_DIR);
        FileUtils.cleanDirectory(FileUtils.ALLURE_RESULTS_DIR);
        FileUtils.cleanDirectory(FileUtils.ALLURE_REPORT_DIR);

        if (!Files.exists(Paths.get(FileUtils.ALLURE_RESULTS_DIR))) {
            throw new IOException("Failed to initialize Allure results directory at: " +
                    Paths.get(FileUtils.ALLURE_RESULTS_DIR).toAbsolutePath());
        }
    }

    /* ------------------- Test Setup ------------------- */

    private void initializeTestLogging(String testName) {
        String logFilePath = String.format("%s/%s-%s.log",
                FileUtils.LOG_DIR,
                testName,
                LocalDateTime.now().format(LOG_TIMESTAMP_FORMATTER));

        try {
            PrintStream logStream = new PrintStream(new FileOutputStream(logFilePath));
            testLogStreams.put(testName, logStream);
        } catch (FileNotFoundException e) {
            throw new TestSetupException("Failed to create test log file: " + logFilePath, e);
        }
    }

    private void configureRequestSpecification(String testName) {
        PrintStream testLogStream = testLogStreams.get(testName);

        requestSpecification = new RequestSpecBuilder()
                .setBaseUri(baseURI)
                .setContentType(ContentType.JSON)
                .addHeader("Accept", "*/*")
                .addFilters(createLoggingFilters(testLogStream))
                .setConfig(restAssuredConfig)
                .build();
    }

    private List<Filter> createLoggingFilters(PrintStream logStream) {
        return Arrays.asList(
                new AllureRestAssured(),
                new RequestLoggingFilter(LogDetail.ALL, logStream),
                new ResponseLoggingFilter(LogDetail.ALL, logStream),
                new RequestLoggingFilter(LogDetail.ALL, System.out),
                new ResponseLoggingFilter(LogDetail.ALL, System.out)
        );
    }

    /* ------------------- Reporting ------------------- */

    private void generateTestReports() {
        try {
            ReportUtils.generateAllureReport();
            String htmlSummary = ReportUtils.getEnhancedSummaryHtml();
            EmailUtils.sendHtmlReportSummary(htmlSummary);
            FileUtils.zipFolder(FileUtils.ALLURE_REPORT_DIR, FileUtils.ALLURE_REPORT_ZIP);
        } catch (Exception e) {
            System.err.println("Error during report generation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ------------------- Cleanup ------------------- */

    private void cleanupTestResources() {
        closeLogStreams();
        closeDatabaseConnection();
    }

    private void closeLogStreams() {
        testLogStreams.values().forEach(stream -> {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                System.err.println("Error closing log stream: " + e.getMessage());
            }
        });
        testLogStreams.clear();
    }

    private void closeDatabaseConnection() {
        try {
            DBConnection.closeConnection();
        } catch (Exception e) {
            System.err.println("Failed to close database connection: " + e.getMessage());
        }
    }

    /* ------------------- Assertion Utilities ------------------- */

    protected <T> void assertPositiveResponse(Response response, Class<T> responseType) {
        response.then()
                .statusCode(200)
                .body("data.isValid", equalTo(true));

        ValidationResponse<T> vr = ValidationUtils.extractValidationResponse(response, responseType);
        Assert.assertTrue(vr.isValid(), "Response should be valid");
        Assert.assertNotNull(vr.getRow(), "Response row should not be null");
    }

    protected void assertNegativeResponse(Response response, String expectedField, String expectedMessagePart) {
        ValidationUtils.assertInvalidResponseStructure(response);

        // More flexible assertion
        if (expectedMessagePart != null) {
            List<ValidationError> errors = ValidationUtils.extractValidationErrors(response);
            boolean found = errors.stream().anyMatch(e ->
                    (expectedField == null || e.getColumn().equalsIgnoreCase(expectedField)) &&
                            e.getMessage().toLowerCase().contains(expectedMessagePart.toLowerCase()));

            Assert.assertTrue(found, "Expected error containing '" + expectedMessagePart +
                    "' not found in errors: " + errors);
        }
    }

    protected void assertNegativeResponse(Response response) {
        assertNegativeResponse(response, null, null);
    }

    /* ------------------- Custom Exceptions ------------------- */

    private static class TestInfrastructureException extends RuntimeException {
        public TestInfrastructureException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class TestSetupException extends RuntimeException {
        public TestSetupException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
