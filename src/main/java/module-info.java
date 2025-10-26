module demo2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;
    requires jakarta.mail; // <-- Ensure this line is present

    opens sample.demo2.ai to javafx.fxml;
    exports sample.demo2.ai;
    opens sample.demo2.main to javafx.fxml;

    // You might also have some exports, which is fine
    exports sample.demo2.main;
    exports sample.demo2.entity;
    opens sample.demo2.entity to javafx.fxml;
    exports sample.demo2.object;
    opens sample.demo2.object to javafx.fxml;
    exports sample.demo2.tile_interactive;
    opens sample.demo2.tile_interactive to javafx.fxml;
    exports sample.demo2.environment;
    opens sample.demo2.environment to javafx.fxml;
    exports sample.demo2.data to  javafx.fxml;
    opens sample.demo2.data;
    exports sample.demo2.monster to  javafx.fxml;
    opens sample.demo2.monster;
    exports sample.demo2.tile to  javafx.fxml;
    opens sample.demo2.tile;
}