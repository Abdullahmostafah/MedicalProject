package Utils;

import java.sql.*;
import java.util.Properties;

// Utility class for managing database connections
public final class DBConnection {
    // Singleton pattern to ensure only one connection instance exists
    private static volatile Connection connection;
    // Database connection parameters read from configuration
    private static final String DB_URL = ConfigReaderWriter.getPropKey("db.url");
    // Database user name read from configuration
    private static final String DB_USER = ConfigReaderWriter.getPropKey("db.user");
    // Encrypted database password and secret key for decryption
    private static final String ENCRYPTED_PASSWORD = ConfigReaderWriter.getPropKey("db.password");
    // Secret key used for decrypting the database password
    private static final String SECRET_KEY = ConfigReaderWriter.getPropKey("db.secret");

    // Private constructor to prevent instantiation
    private DBConnection() {
    }

    // Retrieves a database connection, creating it if it doesn't already exist
    public static Connection getConnection() throws SQLException {
        // Check if the connection is null or closed, and create a new one if necessary
        if (connection == null || connection.isClosed()) {
            // Synchronize on the class to ensure thread safety when creating the connection
            synchronized (DBConnection.class) {
                // Double-check if the connection is still null or closed after acquiring the lock
                if (connection == null || connection.isClosed()) {
                    // Decrypt the password using the secret key
                    String decryptedPassword = ConfigReaderWriter.getDecryptedPropKey("db.password", SECRET_KEY);
                    // Load the JDBC driver (optional, depending on your JDBC version)
                    Properties props = new Properties();
                    // Set the user and decrypted password in properties
                    props.put("user", DB_USER);
                    props.put("password", decryptedPassword);
                    // Establish the connection to the database
                    connection = DriverManager.getConnection(DB_URL, props);
                    // Set auto-commit to false for transaction control
                    connection.setAutoCommit(false); // Enable transaction control
                }
            }
        }
        // Return the established connection
        return connection;
    }

    // Closes the database connection if it is open
    public static void closeConnection() {
        // Check if the connection is not null and is open before attempting to close it
        if (connection != null) {
            // Attempt to close the connection
            try {
                // Check if the connection is not already closed
                if (!connection.isClosed()) {
                    // Close the connection
                    connection.close();
                }
            }
            // Catch any SQL exceptions that may occur during closing
            catch (SQLException e) {
                // Log the error message to standard error output
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    // Commits the current transaction
    public static void commit() throws SQLException {
        // Check if the connection is not null and auto-commit is disabled
        if (connection != null && !connection.getAutoCommit()) {
            // Commit the current transaction
            connection.commit();
        }
    }

    // Rolls back the current transaction if auto-commit is disabled
    public static void rollback() {
        // Check if the connection is not null
        if (connection != null) {
            // Attempt to roll back the transaction
            try {
                // Check if auto-commit is disabled before rolling back
                if (!connection.getAutoCommit()) {
                    // Roll back the current transaction
                    connection.rollback();
                }
                // Catch any SQL exceptions that may occur during rollback
            } catch (SQLException e) {
                // Log the error message to standard error output
                System.err.println("Error during rollback: " + e.getMessage());
            }
        }
    }

    // Executes a SQL query and returns a ResultSet
    public static ResultSet executeQuery(String sql) throws SQLException {
        // Get a connection to the database
        Connection conn = getConnection();
        // Create a statement and execute the query
        return conn.createStatement().executeQuery(sql);
    }

    // Executes a SQL update (INSERT, UPDATE, DELETE) and returns the number of affected rows
    public static int executeUpdate(String sql) throws SQLException {
        // Get a connection to the database
        Connection conn = getConnection();
        // Create a statement and execute the update
        return conn.createStatement().executeUpdate(sql);
    }
}