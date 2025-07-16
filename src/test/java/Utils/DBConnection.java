package Utils;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DBConnection {

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = ConfigReaderWriter.getPropKey("db.url");
            String user = ConfigReaderWriter.getPropKey("db.user");
            String encryptedPassword = ConfigReaderWriter.getPropKey("db.password");
            String secret = ConfigReaderWriter.getPropKey("db.secret");

            String decryptedPassword = decryptAES(encryptedPassword, secret);
            connection = DriverManager.getConnection(url, user, decryptedPassword);
        }
        return connection;
    }

    private static String decryptAES(String encryptedText, String secret) {
        try {
            byte[] keyBytes = Arrays.copyOf(secret.getBytes(StandardCharsets.UTF_8), 16); // AES-128
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decodedValue = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedVal = cipher.doFinal(decodedValue);

            return new String(decryptedVal, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt DB password", e);
        }
    }


    public static ResultSet executeQuery(String sql) throws SQLException {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY
            );
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query", e);
        }
    }

    public static void printResultSetAsTable(ResultSet rs) {
        try {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            int[] widths = new int[columnCount + 1];
            String[][] rows = new String[1000][columnCount + 1];
            int rowCount = 0;

            for (int i = 1; i <= columnCount; i++) {
                widths[i] = meta.getColumnName(i).length();
            }

            while (rs.next()) {
                rowCount++;
                for (int i = 1; i <= columnCount; i++) {
                    String value = String.valueOf(rs.getObject(i));
                    rows[rowCount][i] = value;
                    widths[i] = Math.max(widths[i], value.length());
                }
            }

            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-" + (widths[i] + 2) + "s", meta.getColumnName(i));
            }
            System.out.println();

            for (int i = 1; i <= columnCount; i++) {
                System.out.print("-".repeat(widths[i] + 2));
            }
            System.out.println();

            for (int r = 1; r <= rowCount; r++) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.printf("%-" + (widths[i] + 2) + "s", rows[r][i]);
                }
                System.out.println();
            }

        } catch (Exception e) {
            throw new RuntimeException("Error printing ResultSet as table", e);
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close DB connection", e);
        }
    }
}