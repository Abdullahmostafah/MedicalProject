package Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public final class ConfigReaderWriter {

    private static final String CONFIG_PATH = "src/test/resources/config.properties";
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties: " + e.getMessage(), e);
        }
    }

    private ConfigReaderWriter() {
    }

    public static String getPropKey(String key) {
        return properties.getProperty(key);
    }

    public static synchronized void setPropKey(String key, String value) {
        properties.setProperty(key, value);
        try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH)) {
            properties.store(fos, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update config.properties", e);
        }
    }
}