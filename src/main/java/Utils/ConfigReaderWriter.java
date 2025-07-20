package Utils;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

// Thread-safe configuration manager for reading and writing key-value pairs from configuration files.
public final class ConfigReaderWriter {
    // Path to the configuration file
    private static final String CONFIG_PATH = "src/test/resources/config.properties";

    // Properties object to hold loaded key-value pairs
    private static final Properties properties = new Properties();

    // Lock to ensure thread-safe access to properties
    private static final ReentrantLock lock = new ReentrantLock();

    // Static initializer to load the properties file once on class load
    static {
        // Load properties from the configuration file
        loadProperties();
    }

    // Private constructor to prevent instantiation
    private ConfigReaderWriter() {
    }

    // Loads properties from the configuration file in a thread-safe manner.
    private static void loadProperties() {
        // Ensure thread safety while loading properties
        lock.lock();
        // Initialize the properties object
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            // Load properties from the specified file
            properties.load(fis);

        } // Catch any IO exceptions that may occur during file operations
        catch (IOException e) {
            // Wrap the IOException in a custom exception for better error handling
            throw new ConfigurationException("Failed to load config.properties", e);
        }// Ensure the lock is released even if an exception occurs
        finally {
            // Release the lock after loading properties
            lock.unlock();
        }
    }

    // Retrieves the plain value of a configuration property by key.
    public static String getPropKey(String key) {
        // Ensure thread-safe access to properties
        lock.lock();
        // Check if the key exists in the properties
        try {
            //
            return properties.getProperty(key);
        }
        //
        finally {
            //
            lock.unlock();
        }
    }

    //Retrieves and decrypts the value of an encrypted configuration property.
    public static String getDecryptedPropKey(String key, String secretKey) {

        String encryptedValue = getPropKey(key);

        try {

            return AESUtils.decrypt(encryptedValue, secretKey);

        } catch (Exception e) {

            throw new ConfigurationException("Failed to decrypt property: " + key, e);
        }
    }

    // Sets a configuration property by key with a plain value.
    public static void setPropKey(String key, String value) {

        lock.lock();

        try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH)) {

            properties.setProperty(key, value);

            properties.store(fos, "Updated configuration");
        } catch (IOException e) {

            throw new ConfigurationException("Failed to update config.properties", e);

        } finally {
            //
            lock.unlock();
        }
    }

    // Sets a configuration property by key with an encrypted value using AES encryption.
    public static void setEncryptedPropKey(String key, String value, String secretKey) {
        // Ensure thread-safe access to properties
        try {
            // Encrypt the value using AES encryption with the provided secret key
            String encryptedValue = AESUtils.encrypt(value, secretKey);
            // Set the encrypted value for the specified key
            setPropKey(key, encryptedValue);

        }
        // Catch any exceptions that may occur during encryption
        catch (Exception e) {
            // Wrap the exception in a custom exception for better error handling
            throw new ConfigurationException("Failed to encrypt property: " + key, e);
        }
    }

    // Custom exception class for handling configuration-related errors.
    private static class ConfigurationException extends RuntimeException {
        // Constructor with a message and cause
        public ConfigurationException(String message, Throwable cause) {
            // Call the superclass constructor with the message and cause
            super(message, cause);
        }
    }


    // Add these methods to your existing ConfigReaderWriter class
    public static void saveTestData(String key, String value) {
        setPropKey("testdata." + key, value); // Prefix with 'testdata.' for organization
    }

    public static String getTestData(String key) {
        return getPropKey("testdata." + key);
    }

    public static void clearTestData(String key) {
        setPropKey("testdata." + key, ""); // Or use properties.remove() if available
    }
}