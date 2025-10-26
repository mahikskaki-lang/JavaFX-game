package sample.demo2.environment;

import sample.demo2.main.GamePanel;

// CHANGED: Import for JavaFX GraphicsContext
import javafx.scene.canvas.GraphicsContext;

public class EnvironmentManager {

    GamePanel gp;
    public Lighting lighting;

    public EnvironmentManager(GamePanel gp) {
        this.gp = gp;
    }
    public void setup() {
        lighting = new Lighting(gp);
    }
    public void update() {
        lighting.update();
    }

    // CHANGED: Method now uses GraphicsContext
    public void draw(GraphicsContext gc) {
        lighting.draw(gc);
    }
}