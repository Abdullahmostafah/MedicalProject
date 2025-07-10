package Base;

import static io.restassured.RestAssured.*;
import static io.restassured.config.SSLConfig.sslConfig;

import Utils.ConfigReaderWriter;
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public abstract class TestBase {

    private static final int TIMEOUT = 30000;
    private static final String BASE_URI = ConfigReaderWriter.getPropKey("base.uri");
    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter LOG_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");

    protected static RestAssuredConfig config;
    protected RequestSpecification requestSpecs;

    @BeforeAll
    public static void globalSetup() {
        baseURI = BASE_URI;

        config = config()
                .sslConfig(sslConfig().relaxedHTTPSValidation().allowAllHostnames())
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", TIMEOUT)
                        .setParam("http.socket.timeout", TIMEOUT));

        enableLoggingOfRequestAndResponseIfValidationFails();

        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    @BeforeEach
    public void setupPerTest(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().map(method -> method.getName()).orElse("unknownTest");
        String timestamp = LocalDateTime.now().format(LOG_TIMESTAMP_FORMATTER);
        String logFileName = LOG_DIR + "/" + methodName + "-" + timestamp + ".log";

        try (PrintStream logFile = new PrintStream(new FileOutputStream(logFileName))) {

            List<Filter> filters = new ArrayList<>();
            filters.add(new AllureRestAssured());
            filters.add(new RequestLoggingFilter(LogDetail.ALL, logFile));
            filters.add(new ResponseLoggingFilter(LogDetail.ALL, logFile));
            filters.add(new RequestLoggingFilter(LogDetail.ALL)); // console
            filters.add(new ResponseLoggingFilter(LogDetail.ALL)); // console

            requestSpecs = new RequestSpecBuilder()
                    .setBaseUri(BASE_URI)
                    .setContentType(ContentType.JSON)
                    .addFilters(filters)
                    .setConfig(config)
                    .build();

            requestSpecification = requestSpecs;
            // ✅ Attach the log file to Allure after test runs
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    File file = new File(logFileName);
                    if (file.exists()) {
                        Allure.addAttachment("Log - " + methodName, new FileInputStream(file));
                    }
                } catch (Exception e) {
                    System.err.println("Could not attach log to Allure: " + e.getMessage());
                }
            }));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize requestSpec for test: " + methodName, e);
        }
    }
}
