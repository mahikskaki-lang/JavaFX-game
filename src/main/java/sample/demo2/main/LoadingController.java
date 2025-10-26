package sample.demo2.main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class LoadingController {

    @FXML
    private ImageView backgroundGifView;
    @FXML
    private Text loadingText;
    @FXML
    private ProgressBar progressBar;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // This method is called by JavaFX after the FXML is loaded
    public void initialize() {
        // Load your animated GIF
        Image gif = new Image(getClass().getResourceAsStream("/sample/demo2/mahim.gif"));
        backgroundGifView.setImage(gif);

        // Start the loading simulation
        startLoadingSimulation();
    }

    private void startLoadingSimulation() {
        // Run the loading process on a background thread to keep the UI responsive
        new Thread(() -> {
            try {
                // Simulate loading different parts of the game
                updateProgress(0.0, "Initializing systems...");
                Thread.sleep(1000);

                updateProgress(0.3, "Loading assets...");
                Thread.sleep(1500);

                updateProgress(0.7, "Connecting to server...");
                Thread.sleep(1000);

                updateProgress(1.0, "Done!");
                Thread.sleep(500);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // When loading is finished, switch to the login scene on the UI thread
                Platform.runLater(this::switchToLoginScene);
            }
        }).start();
    }

    // Helper method to update the UI from the background thread
    private void updateProgress(double progress, String text) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            loadingText.setText(text);
        });
    }

    private void switchToLoginScene() {
        try {
            URL fxmlLocation = getClass().getResource("/sample/demo2/login.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setStage(stage);

            stage.setScene(new Scene(root));
            stage.setTitle("All Of Us Are Dead - Login");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}