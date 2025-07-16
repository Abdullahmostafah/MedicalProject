package Base;

import static io.restassured.RestAssured.*;
import static io.restassured.config.SSLConfig.sslConfig;

import Utils.*;
import io.qameta.allure.Allure;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.mail.MessagingException;
import org.testng.annotations.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class TestBase {

    private static final int TIMEOUT = 30000;
    private static final String BASE_URI = ConfigReaderWriter.getPropKey("base.uri");
    private static final DateTimeFormatter LOG_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");

    protected static RestAssuredConfig config;
    protected RequestSpecification requestSpecs;

    private final Map<String, PrintStream> logStreams = new HashMap<>();

    @BeforeClass
    public static void globalSetup() throws IOException {
        try {
            DBConnection.getConnection();
            System.out.println("✅ DB Connection successful");
        } catch (Exception ex) {
            throw new RuntimeException("❌ DB Connection failed: " + ex.getMessage(), ex);
        }

        try (Socket socket = new Socket("localhost", 5251)) {
            System.out.println("✅ Backend is up on port 5251");
        } catch (IOException e) {
            throw new RuntimeException("❌ Backend is NOT running on port 5251", e);
        }
        baseURI = BASE_URI;

        config = config()
                .sslConfig(sslConfig().relaxedHTTPSValidation().allowAllHostnames())
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", TIMEOUT)
                        .setParam("http.socket.timeout", TIMEOUT));

        enableLoggingOfRequestAndResponseIfValidationFails();

        FileUtils.cleanDirectory(FileUtils.LOG_DIR);
        FileUtils.cleanDirectory(FileUtils.ALLURE_RESULTS_DIR);
        FileUtils.cleanDirectory(FileUtils.ALLURE_REPORT_DIR);
    }

    @BeforeMethod
    public void setupPerTest(Method method) {
        String methodName = method.getName();
        String timestamp = LocalDateTime.now().format(LOG_TIMESTAMP_FORMATTER);
        String logFileName = FileUtils.LOG_DIR + "/" + methodName + "-" + timestamp + ".log";

        try {
            PrintStream logFile = new PrintStream(new FileOutputStream(logFileName, false));
            logStreams.put(methodName, logFile);

            List<Filter> filters = List.of(
                    new AllureRestAssured(),
                    new RequestLoggingFilter(LogDetail.ALL, logFile),
                    new ResponseLoggingFilter(LogDetail.ALL, logFile),
                    new RequestLoggingFilter(LogDetail.ALL, System.out),
                    new ResponseLoggingFilter(LogDetail.ALL, System.out)
            );

            requestSpecs = new RequestSpecBuilder()
                    .setBaseUri(BASE_URI)
                    .setContentType(ContentType.JSON)
                    .addHeader("Accept", "*/*")
                    .addFilters(filters)
                    .setConfig(config)
                    .build();

            requestSpecification = requestSpecs;

            FileUtils.attachLogToAllure("Log - " + methodName, logFileName);

        } catch (IOException e) {
            throw new RuntimeException("Failed to create log file for test: " + methodName, e);
        }
    }

    @AfterClass
    public static void tearDownAll() throws MessagingException, IOException {
        String recipients = ConfigReaderWriter.getPropKey("report.recipients");
        ReportUtils.generateAndSendReport();
        EmailUtils.sendEmailWithAttachment(recipients, "Automated Test Report", "Attached is the Allure report.", FileUtils.ALLURE_REPORT_ZIP);
        DBConnection.closeConnection();
    }
}
