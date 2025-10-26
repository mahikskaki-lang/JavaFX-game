package sample.demo2.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the Loading Screen first
        URL fxmlLocation = getClass().getResource("/sample/demo2/loadingScreen.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        // Get the controller and pass the stage to it
        LoadingController controller = loader.getController();
        controller.setStage(primaryStage);

        // Configure and show the stage
        Scene scene = new Scene(root);
        primaryStage.setTitle("All Of Us Are Dead-Powered by Team_Debug_Dragons");
        primaryStage.setResizable(false);
        try (InputStream is = getClass().getResourceAsStream("/player/boy_down_1.png")) {
            if (is != null) {
                primaryStage.getIcons().add(new Image(is));
            }
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}