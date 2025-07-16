package Utils;

import io.qameta.allure.Allure;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static Utils.EmailUtils.sendEmailWithAttachment;

public class FileUtils {

    public static final String LOG_DIR = "target/logs";
    public static final String ALLURE_RESULTS_DIR = "target/allure-results";
    public static final String ALLURE_REPORT_DIR = "target/allure-report";
    public static final String ALLURE_REPORT_ZIP = "target/allure-report.zip";

    // =========================
// Log & Allure Attachment
// =========================
    public static void cleanDirectory(String pathStr) throws IOException {
        Path path = Paths.get(pathStr);
        if (Files.exists(path)) {
            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectories(path);
    }

    public static void attachLogToAllure(String name, String filePath) {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            Allure.addAttachment(name, inputStream);
        } catch (IOException e) {
            System.err.println("Failed to attach log to Allure: " + e.getMessage());
        }
    }

    // =========================
// Allure Report Utilities
// =========================
    public static void generateAllureReport() {
        try {
            Process process = Runtime.getRuntime().exec("allure generate target/allure-results -o target/allure-report --clean");
            process.waitFor();
            System.out.println("✅ Allure report generated.");
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Failed to generate Allure report: " + e.getMessage());
        }
    }

    public static void zipFolder(String sourceFolderPath, String zipFilePath) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(zipFilePath);
                ZipOutputStream zos = new ZipOutputStream(fos)
        ) {
            File folder = new File(sourceFolderPath);
            zipFileRecursive(folder, folder.getName(), zos);
        }
    }

    private static void zipFileRecursive(File fileToZip, String fileName, ZipOutputStream zos) throws IOException {
        if (fileToZip.isHidden()) return;

        if (fileToZip.isDirectory()) {
            if (!fileName.endsWith("/")) fileName += "/";
            zos.putNextEntry(new ZipEntry(fileName));
            zos.closeEntry();
            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File child : children) {
                    zipFileRecursive(child, fileName + child.getName(), zos);
                }
            }
            return;
        }

        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }
        }
    }

    public static void sendEmailWithReport(String toEmail) {
        try {
            generateAllureReport();
            zipFolder(ALLURE_REPORT_DIR, ALLURE_REPORT_ZIP);
            sendEmailWithAttachment(toEmail, "Automated Test Report", "Attached is the Allure report.", ALLURE_REPORT_ZIP);
        } catch (Exception e) {
            System.err.println("❌ Failed to send report: " + e.getMessage());
        }
    }

}