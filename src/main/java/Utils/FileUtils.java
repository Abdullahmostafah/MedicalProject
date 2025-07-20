package Utils;

import java.io.*;
import java.nio.file.*;
import java.util.Comparator;
import java.util.zip.*;

// Utility class for file operations such as cleaning directories, zipping folders, and checking directory writability.
public final class FileUtils {

    // Directory configurations
    public static final String LOG_DIR = "target/logs";
    public static final String ALLURE_RESULTS_DIR = "target/allure-results";
    public static final String ALLURE_REPORT_DIR = "target/allure-report";
    public static final String ALLURE_REPORT_ZIP = "target/ReportSummary.zip";

    // Private constructor to prevent instantiation of this utility class
    private FileUtils() {
    }

    // Cleans a directory by deleting all its contents and recreating it.
    public static void cleanDirectory(String pathStr) throws IOException {
        // Convert string path to Path object
        Path path = Paths.get(pathStr);

        // Check if the path is absolute, if not, convert it to absolute path
        if (Files.exists(path)) {
            // If the path exists, delete all files and directories within it
            Files.walk(path)
                    // Filter out the root directory itself
                    .sorted(Comparator.reverseOrder())
                    // Delete all files and directories
                    .map(Path::toFile)
                    // Exclude the root directory from deletion
                    .forEach(File::delete);
        }

        // Create fresh directory
        Files.createDirectories(path);

        // Check if the directory was created successfully
        if (!Files.exists(path)) {
            // If the directory was not created successfully, throw an IOException
            throw new IOException("Failed to create directory: " + path.toAbsolutePath());
        }
    }

    // Zips the contents of a folder into a zip file.
    public static void zipFolder(String sourceFolderPath, String zipFilePath) throws IOException {
        // Convert the source folder path to a Path object
        Path sourcePath = Paths.get(sourceFolderPath);
        // Check if the source folder exists
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            // If the source folder does not exist, throw an IOException
            Files.walk(sourcePath)
                    // Filter out directories to only include files
                    .filter(path -> !Files.isDirectory(path))
                    // For each file, create a ZipEntry and copy the file content to the zip output stream
                    .forEach(path -> {
                        // Use try-with-resources to ensure the ZipEntry is closed properly
                        try {
                            // Create a relative path for the file within the zip
                            String relativePath = sourcePath.relativize(path).toString();
                            // Create a new ZipEntry for the file
                            zos.putNextEntry(new ZipEntry(relativePath));
                            // Copy the file content to the zip output stream
                            Files.copy(path, zos);
                            // Close the current ZipEntry
                            zos.closeEntry();
                            // Log the file being added to the zip
                        } catch (IOException e) {
                            // If an IOException occurs, log the error and rethrow it as an UncheckedIOException
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }

    // Checks if directory is writable
    public static boolean isDirectoryWritable(String path) {
        // Convert the string path to a Path object
        Path dirPath = Paths.get(path);
        // Check if the directory exists and is a directory
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            // If the directory does not exist or is not a directory, return false
            return false;
        }
        // Attempt to create a temporary file in the directory to check writability
        try {
            // Create a temporary file in the directory
            Path testFile = dirPath.resolve("write-test.tmp");
            // Create the file
            Files.createFile(testFile);
            // Delete the test file after creation to clean up
            Files.delete(testFile);
            // If the file creation and deletion were successful, return true
            return true;
        }
        // Catch any IO exceptions that may occur during file creation or deletion
        catch (IOException e) {
            // Log the error message to standard error output
            return false;
        }
    }
}