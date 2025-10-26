package sample.demo2.main;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.Random;






public class EmailService {

    // IMPORTANT: Use your email and the 16-digit App Password you generated
    private static final String SENDER_EMAIL = "YourEmail";
    private static final String SENDER_PASSWORD = "YourAppPasswordHere"; // Replace with your actual App Password

    public static String generateOtp() {
        // Generates a 6-digit OTP
        return String.format("%06d", new Random().nextInt(1000000));
    }

    public static boolean sendOtpEmail(String recipientEmail, String otp) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your Game Verification OTP");
            message.setText("Welcome to the Game!\n\nYour One-Time Password (OTP) is: " + otp +
                    "\n\nThis code is valid for a short time. Please do not share it with anyone.");

            Transport.send(message);
            System.out.println("OTP email sent successfully to " + recipientEmail);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Failed to send OTP email.");
            return false;
        }
    }
}