package Utils;

import java.util.Scanner;

// Command-line tool for encrypting passwords using AES.
public class PasswordEncryptor {
    public static void main(String[] args) {
        // Display title for the utility
        System.out.println("AES Password Encryption Tool");
        // Initialize a scanner to read user input from the console
        Scanner scanner = new Scanner(System.in);
        // Prompt the user to enter the plain (unencrypted) password
        System.out.println("Enter the plain password to encrypt:");
        String password = scanner.nextLine();
        // Prompt the user to enter a secret key (used for AES encryption)
        System.out.println("Enter the secret key (any length will be trimmed or padded to 16 bytes):");
        String secretKey = scanner.nextLine();
        // Validate that the secret key is not empty
        if (secretKey == null || secretKey.trim().isEmpty()) {

            System.err.println("Error: Secret key cannot be empty.");
            return; // Exit the program early
        }

        try {
            // Call the AESUtils.encrypt method to encrypt the password using the provided secret key
            String encryptedPassword = AESUtils.encrypt(password, secretKey);
            // Display the result to the user
            System.out.println("\n Encrypted Password:\n" + encryptedPassword);
            //
            System.out.println("\n Copy and paste this into your config.properties under the correct key.");

        } catch (Exception e) {
            // Print any error that occurs during encryption
            System.err.println(" Encryption failed: " + e.getMessage());
        }
    }
}
