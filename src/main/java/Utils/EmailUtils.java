package Utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.time.LocalDate;
import java.util.Properties;

// Utility class for sending emails with HTML content and attachments
public final class EmailUtils {

    // Private constructor to prevent instantiation
    private EmailUtils() {
    }

    // Sends an HTML report summary via email
    public static void sendHtmlReportSummary(String htmlSummary) {
        // Validate input
        try {
            // Get username from configuration
            String username = ConfigReaderWriter.getPropKey("email.username");
            // Get recipients from configuration
            String recipients = ConfigReaderWriter.getPropKey("report.recipients");
            // Create email message with configured username and recipients
            Message message = createEmailMessage(username, recipients);
            // Set subject and content type
            message.setSubject("Test Execution Report - " + LocalDate.now());
            // Set HTML content
            message.setContent(htmlSummary, "text/html; charset=utf-8");
            // Send the email
            Transport.send(message);
            // Log success message
            System.out.println("Email sent successfully to: " + recipients);
            // Handle any exceptions that occur during email sending
        } catch (Exception e) {
            // Log the error and throw a custom exception
            throw new EmailException("Failed to send email", e);
        }
    }

    // Sends an HTML report with an attachment via email
    public static void sendWithAttachment(String htmlContent, String attachmentPath) throws Exception {
        // get username from configuration
        String username = ConfigReaderWriter.getPropKey("email.username");
        // get recipients from configuration
        String recipients = ConfigReaderWriter.getPropKey("report.recipients");
        // Create email message with configured username and recipients
        Message message = createEmailMessage(username, recipients);
        // Set subject and content type
        message.setSubject("Test Report with Attachment - " + LocalDate.now());

        // Create a multipart message to hold both text and attachment
        MimeBodyPart textPart = new MimeBodyPart();
        // Set HTML content for the text part
        textPart.setContent(htmlContent, "text/html");
        // Create a new MimeBodyPart for the attachment
        MimeBodyPart attachmentPart = new MimeBodyPart();
        // Set the attachment file
        attachmentPart.attachFile(attachmentPath);

        Multipart multipart = new MimeMultipart();
        // Add both parts to the multipart message
        multipart.addBodyPart(textPart);
        // Add the attachment part to the multipart message
        multipart.addBodyPart(attachmentPart);
        // Set the multipart content to the message
        message.setContent(multipart);
        // Send the email with attachment
        Transport.send(message);
    }

    // Creates a new email message with the specified username and recipients
    private static Message createEmailMessage(String username, String recipients) throws Exception {
        // Decrypt the email password using the secret key
        String decryptedPassword = ConfigReaderWriter.getDecryptedPropKey(
                "email.password",
                // Get the secret key from configuration
                ConfigReaderWriter.getPropKey("email.secret")
        );

        // Create a session with SMTP properties and authentication
        Session session = Session.getInstance(
                // Get SMTP properties from configuration
                getSmtpProperties(),
                // Create an authenticator for SMTP authentication
                new Authenticator() {
                    // Override the getPasswordAuthentication method to provide credentials
                    protected PasswordAuthentication getPasswordAuthentication() {
                        // Return a new PasswordAuthentication object with the username and decrypted password
                        return new PasswordAuthentication(username, decryptedPassword);
                    }
                }
        );

        // Create a new MimeMessage with the session
        Message message = new MimeMessage(session);
        // Set the sender's email address
        message.setFrom(new InternetAddress(username));
        // Set the recipients' email addresses
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        // Set the reply-to address to the sender's email
        return message;
    }

    // Retrieves SMTP properties from the configuration
    private static Properties getSmtpProperties() {
        // Create a new Properties object to hold SMTP configuration
        Properties props = new Properties();
        // Load SMTP properties from the configuration file
        props.put("mail.smtp.auth", ConfigReaderWriter.getPropKey("mail.smtp.auth"));
        // Enable STARTTLS for secure communication
        props.put("mail.smtp.starttls.enable", ConfigReaderWriter.getPropKey("mail.smtp.starttls.enable"));
        // Set the SMTP host from the configuration
        props.put("mail.smtp.host", ConfigReaderWriter.getPropKey("mail.smtp.host"));
        // Set the SMTP port from the configuration
        props.put("mail.smtp.port", ConfigReaderWriter.getPropKey("mail.smtp.port"));
        // Set the SMTP SSL port from the configuration
        return props;
    }

    // Custom exception class for email-related errors
    private static class EmailException extends RuntimeException {
        // Constructor with a message
        public EmailException(String message, Throwable cause) {
            // Call the superclass constructor
            super(message, cause);
        }
    }
}