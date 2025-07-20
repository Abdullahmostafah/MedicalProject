package Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Arrays;

// Utility class for AES encryption and decryption
// This class provides methods to encrypt and decrypt strings using AES algorithm with a secret key.
// It uses AES in ECB mode with PKCS5 padding.
// Note: ECB mode is not recommended for sensitive data due to its security vulnerabilities.
public final class AESUtils {
    // Constant for AES algorithm with ECB mode and PKCS5 padding
    private static final String AES_ALGORITHM = "AES/ECB/PKCS5Padding";

    // Private constructor to prevent instantiation of this utility class
    private AESUtils() {
    }

    // Generates a SecretKeySpec from the provided secret string.
    private static SecretKeySpec getSecretKey(String secret) {
        // Ensure the secret key is exactly 16 bytes long by truncating or padding with zeros
        byte[] keyBytes = Arrays.copyOf(secret.getBytes(StandardCharsets.UTF_8), 16);
        // Create a SecretKeySpec using the key bytes and specify the AES algorithm
        return new SecretKeySpec(keyBytes, "AES");
    }

    // Encrypts a string using AES with the provided secret key.
    public static String encrypt(String data, String secretKey) throws Exception {
        // Generate a SecretKeySpec from the provided secret key
        SecretKeySpec key = getSecretKey(secretKey);
        // Create a Cipher instance for AES encryption
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        // Initialize the cipher in encryption mode with the secret key
        cipher.init(Cipher.ENCRYPT_MODE, key);
        // Convert the input string to bytes and encrypt it
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        // Encode the encrypted bytes to a Base64 string for easy storage and transmission
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decrypts an encrypted Base64 string using AES with the provided secret key.
    public static String decrypt(String encryptedData, String secretKey) throws Exception {
        // Generate a SecretKeySpec from the provided secret key
        SecretKeySpec key = getSecretKey(secretKey);
        // Create a Cipher instance for AES decryption
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        // Initialize the cipher in decryption mode with the secret key
        cipher.init(Cipher.DECRYPT_MODE, key);
        // Decode the Base64 encoded string to get the encrypted bytes
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        // Decrypt the bytes using the cipher
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        // Convert the decrypted bytes back to a string using UTF-8 encoding
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}