package Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

import static Utils.FileUtils.*;

// Utility class for generating Allure reports and HTML summaries from test results
public final class ReportUtils {
    // Mapper for JSON processing
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    // Private constructor to prevent instantiation
    private ReportUtils() {
    }

    // Generates Allure report from test results
    public static void generateAllureReport() {
        try {
            // Get the operating system name to determine the command to run
            String osName = System.getProperty("os.name").toLowerCase();
            // Determine the command to run based on the OS
            String allureCmd = osName.contains("win") ? "allure-wrapper.bat" : "allure";
            // Create a ProcessBuilder to run the Allure command
            ProcessBuilder builder = new ProcessBuilder(allureCmd, "generate", ALLURE_RESULTS_DIR, "-o", ALLURE_REPORT_DIR, "--clean");
            // Set the working directory to the current user's directory
            builder.directory(new File(System.getProperty("user.dir")));
            // Set the environment variables for the process
            builder.inheritIO();
            // Start the process to generate the Allure report
            Process process = builder.start();
            // Wait for the process to complete and get the exit code
            int exitCode = process.waitFor();
            // Check if the exit code indicates success
            if (exitCode != 0) {
                // Exception if Allure report generation failed
                throw new ReportGenerationException("Allure report generation failed with code: " + exitCode, null);
            }
            // Log the successful generation of the Allure report
            System.out.println("Allure report generated at: " + Paths.get(ALLURE_REPORT_DIR).toAbsolutePath());
            // Catch any exceptions that occur during report generation
        } catch (Exception e) {
            // Log the error and throw a custom exception
            throw new ReportGenerationException("Failed to generate Allure report", e);
        }
    }

    // Generates an HTML summary of the Allure report
    public static String getEnhancedSummaryHtml() {
        // Check if the Allure report directory exists
        try {
            // Get the path to the Allure summary JSON file
            Path summaryPath = Paths.get(ALLURE_REPORT_DIR, "widgets", "summary.json");
            // Get the summary JSON file
            return generateHtmlSummary(summaryPath.toFile());
            // Catch any exceptions that occur during summary generation
        } catch (Exception e) {
            // Log the error and return an error HTML snippet
            return createErrorHtml("Failed to generate report summary", e);
        }
    }

    //Generates HTML summary from Allure JSON data
    private static String generateHtmlSummary(File summaryJson) throws IOException {
        // Set up the JSON mapper to read the summary JSON file
        JsonNode root = jsonMapper.readTree(summaryJson);
        // Set the statistics node to the "statistic" field in the JSON
        JsonNode statsNode = root.path("statistic");
        // Set the timing node to the "time" field in the JSON
        JsonNode timingNode = root.path("time");

        // Extract test statistics
        TestStats testStats = extractTestStats(statsNode);
        // Extract test timing information
        TestTiming testTiming = extractTestTiming(timingNode);
        // return the generated HTML summary
        return buildSummaryHtml(testStats, testTiming);
    }

    // Extracts test statistics from the JSON node
    private static TestStats extractTestStats(JsonNode statsNode) {
        // Extract total, passed, failed, and skipped counts from the JSON node
        return new TestStats(statsNode.path("total").asInt(), statsNode.path("passed").asInt(), statsNode.path("failed").asInt(), statsNode.path("skipped").asInt());
    }

    // Extracts test timing information from the JSON node
    private static TestTiming extractTestTiming(JsonNode timeNode) {
        // Extract duration, start, and stop times from the JSON node
        long duration = timeNode.path("duration").asLong();
        // Extract start and stop times, defaulting to 0 if not present
        long start = timeNode.path("start").asLong();
        // Extract stop time, defaulting to 0 if not present
        long stop = timeNode.path("stop").asLong();

        // Calculate actual duration if not provided
        if (duration == 0 && start > 0 && stop > start) {
            duration = stop - start;
        }
        // Create TestTiming object with calculated duration and start/stop times
        return new TestTiming(duration, start > 0 ? Instant.ofEpochMilli(start) : null, stop > 0 ? Instant.ofEpochMilli(stop) : null);
    }

    // Builds the HTML summary string with test statistics and timing
    private static String buildSummaryHtml(TestStats stats, TestTiming timing) {
        // Define a formatter for date and time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
        // Generate the HTML summary with test statistics and timing
        return String.format("""
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; margin: 20px; }
                                .summary { border-collapse: collapse; width: 100%%; max-width: 600px; }
                                .summary th, .summary td { padding: 10px; text-align: center; border: 1px solid #ddd; }
                                .passed { color: #4CAF50; }
                                .failed { color: #F44336; }
                                .skipped { color: #FF9800; }
                                .meta { margin-top: 20px; color: #666; }
                            </style>
                        </head>
                        <body>
                            <h2>Test Execution Summary</h2>
                            <table class="summary">
                                <tr>
                                    <th>Total</th>
                                    <th class="passed">Passed</th>
                                    <th class="failed">Failed</th>
                                    <th class="skipped">Skipped</th>
                                    <th>Duration</th>
                                </tr>
                                <tr>
                                    <td>%d</td>
                                    <td class="passed">%d</td>
                                    <td class="failed">%d</td>
                                    <td class="skipped">%d</td>
                                    <td>%s</td>
                                </tr>
                            </table>
                            <div class="meta">
                                <p><b>Start:</b> %s</p>
                                <p><b>End:</b> %s</p>
                                <p><b>Report Generated:</b> %s</p>
                            </div>
                        </body>
                        </html>
                        """,
                // Fill in the summary table with test statistics and timing
                stats.total, stats.passed, stats.failed, stats.skipped, formatDuration(timing.duration), timing.start != null ? formatter.format(timing.start) : "N/A", timing.stop != null ? formatter.format(timing.stop) : "N/A", LocalDateTime.now().format(formatter));
    }

    // Formats duration in minutes and seconds
    private static String formatDuration(long millis) {
        // Convert milliseconds to Duration
        Duration duration = Duration.ofMillis(millis);
        // Format duration as "X min Y sec"
        return String.format("%d min %d sec", duration.toMinutes(), duration.getSeconds() % 60);
    }

    // Generates an error HTML snippet for reporting issues
    private static String createErrorHtml(String message, Exception e) {
        // Create a StringWriter to capture the stack trace
        StringWriter sw = new StringWriter();
        // Print the stack trace to the StringWriter
        e.printStackTrace(new PrintWriter(sw));
        // Return a formatted HTML string with the error message and stack trace
        return String.format("""
                <html>
                <body>
                    <h2 style="color:red">%s</h2>
                    <pre>%s</pre>
                </body>
                </html>
                """, message, sw.toString());
    }

    // Class to hold test statistics
    private static class TestStats {
        final int total;
        final int passed;
        final int failed;
        final int skipped;

        // Constructor to initialize test statistics
        TestStats(int total, int passed, int failed, int skipped) {
            this.total = total;
            this.passed = passed;
            this.failed = failed;
            this.skipped = skipped;
        }
    }

    // Class to hold timing information for tests
    private static class TestTiming {
        final long duration;
        final Instant start;
        final Instant stop;

        // Constructor to initialize timing data
        TestTiming(long duration, Instant start, Instant stop) {
            this.duration = duration;
            this.start = start;
            this.stop = stop;
        }
    }

    // Custom exception for report generation errors
    private static class ReportGenerationException extends RuntimeException {
        // Constructor with message and cause
        public ReportGenerationException(String message, Throwable cause) {
            // Call the superclass constructor
            super(message, cause);
        }
    }
}