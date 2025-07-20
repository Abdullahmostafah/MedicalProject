package Utils;

import java.io.*;
import java.util.Properties;

public class TestDataCache {
    private static final String CACHE_FILE = "target/test-data-cache.properties";
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static synchronized void loadProperties() {
        File file = new File(CACHE_FILE);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                properties.load(reader);
            } catch (IOException e) {
                System.err.println("Error loading test data cache: " + e.getMessage());
            }
        }
    }

    public static synchronized void save(String key, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key and value cannot be null");
        }

        properties.setProperty(key, value);
        try {
            // Ensure target directory exists
            new File("target").mkdirs();
            try (FileWriter writer = new FileWriter(CACHE_FILE)) {
                properties.store(writer, "Test Data Cache");
            }
        } catch (IOException e) {
            System.err.println("Error saving test data cache: " + e.getMessage());
        }
    }

    public static synchronized String get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return properties.getProperty(key);
    }
}