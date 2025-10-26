package sample.demo2.main;

import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Pattern;
import sample.demo2.main.EmailService; // Assuming EmailService is in sample.demo1

public class SignupController {

    @FXML private Label messageLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private Button sendOtpButton;
    @FXML private VBox otpContainer;
    @FXML private TextField otpField;
    @FXML private Button signupButton;
    @FXML private Button backButton;

    private Stage stage;
    private String generatedOtp;
    private static final String ACCOUNTS_FILE = "accounts.txt";

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleSendOtp() {
        String recipientEmail = emailField.getText();

        if (!isValidEmail(recipientEmail)) {
            messageLabel.setText("Please enter a valid email address.");
            return;
        }

        messageLabel.setText("Sending OTP...");

        // Generate the OTP using your service
        this.generatedOtp = EmailService.generateOtp();

        // Run email sending on a background thread to keep the UI responsive
        new Thread(() -> {
            boolean emailSent = EmailService.sendOtpEmail(recipientEmail, generatedOtp);

            // Update the UI back on the main JavaFX thread
            Platform.runLater(() -> {
                if (emailSent) {
                    messageLabel.setText("OTP sent to your email!");
                    otpContainer.setVisible(true);
                    otpContainer.setManaged(true);
                } else {
                    messageLabel.setText("Failed to send OTP. Check console for errors.");
                }
            });
        }).start();
    }

    @FXML
    private void handleSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String enteredOtp = otpField.getText();

        if (username.isEmpty() || password.isEmpty() || enteredOtp.isEmpty()) {
            messageLabel.setText("Please fill in all fields, including OTP.");
            return;
        }

        if (generatedOtp == null || !generatedOtp.equals(enteredOtp)) {
            messageLabel.setText("Invalid OTP. Please try again.");
            return;
        }

        try {
            if (doesUserExist(username)) {
                messageLabel.setText("Username already taken. Please choose another.");
                return;
            }

            try (FileWriter fw = new FileWriter(ACCOUNTS_FILE, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                String hashedPassword = hashPassword(password);
                out.println(username + "," + hashedPassword);

                messageLabel.setText("Successfully Created");
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(event -> loadLoginScene());
                pause.play();
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            messageLabel.setText("Error: Could not create account.");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        return email != null && pat.matcher(email).matches();
    }

    @FXML
    private void loadLoginScene() {
        try {
            URL fxmlLocation = getClass().getResource("/sample/demo2/login.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setStage(stage);
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean doesUserExist(String username) throws IOException {
        File file = new File(ACCOUNTS_FILE);
        if (!file.exists()) {
            return false;
        }
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}