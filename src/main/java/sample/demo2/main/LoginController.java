package sample.demo2.main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter; // --- NEW ---
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;
    @FXML private Button guestButton;
    @FXML private Button signupButton;

    private Stage stage;
    private static final String ACCOUNTS_FILE = "accounts.txt";

    public void initialize() {
        // --- MODIFIED --- Pass null for guests
        guestButton.setOnAction(event -> switchToGameScene(null));
        loginButton.setOnAction(event -> handleLoginButtonClick());
        signupButton.setOnAction(event -> handleSignupButtonClick());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void handleLoginButtonClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        try {
            if (areCredentialsValid(username, password)) {
                messageLabel.setText("Login Successful!");
                saveLastUser(username); // --- NEW --- Save the username for the "Load Game" button
                switchToGameScene(username); // --- MODIFIED --- Pass the username
            } else {
                messageLabel.setText("Invalid username or password.");
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            messageLabel.setText("Error during login.");
        }
    }

    // --- NEW --- This method saves the last successful login to a file.
    private void saveLastUser(String username) {
        try (PrintWriter writer = new PrintWriter("last_login.txt", "UTF-8")) {
            writer.println(username);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not save last logged in user.");
        }
    }

    private boolean areCredentialsValid(String username, String password) throws IOException, NoSuchAlgorithmException {
        File file = new File(ACCOUNTS_FILE);
        if (!file.exists()) {
            return false; // No accounts exist
        }

        String hashedInputPassword = hashPassword(password);

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String storedUsername = parts[0];
                    String storedHashedPassword = parts[1];
                    if (storedUsername.equals(username) && storedHashedPassword.equals(hashedInputPassword)) {
                        return true; // Match found
                    }
                }
            }
        }
        return false; // No match found
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

    private void handleSignupButtonClick() {
        try {
            URL fxmlLocation = getClass().getResource("/sample/demo2/signup.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            SignupController controller = loader.getController();
            controller.setStage(stage);
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Failed to load sign-up screen.");
        }
    }

    // --- MODIFIED --- Method now accepts a username.
    private void switchToGameScene(String username) {
        GamePanel gamePanel = new GamePanel();
        gamePanel.loggedInUsername = username; // null for guest is fine

        Scene gameScene = new Scene(gamePanel);
        gameScene.setFill(javafx.scene.paint.Color.BLACK);

        gameScene.setOnKeyPressed(event -> gamePanel.keyH.keyPressed(event));
        gameScene.setOnKeyReleased(event -> gamePanel.keyH.keyReleased(event.getCode()));
        gameScene.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                gamePanel.handleMouseClick(event.getX(), event.getY());
            }
        });

        stage.setScene(gameScene);
        stage.setTitle("All Of Us Are Dead-Developed by Ritu And Sawon");
        stage.centerOnScreen();

        stage.setOnCloseRequest(event -> {
            System.out.println("Window is closing. Attempting to save game...");
            gamePanel.saveLoad.save();
        });

        // NEW: smooth loading overlay inside the GamePanel
        gamePanel.startGameWithOverlay();
    }
}