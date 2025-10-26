package sample.demo2.environment;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import sample.demo2.main.GamePanel;

import java.util.ArrayList;
import java.util.List;

public class Lighting {

    GamePanel gp;
    // CHANGED: BufferedImage is an AWT class. WritableImage is the JavaFX equivalent.
    WritableImage darknessFilter;
    public int dayCounter;
    public float filterAlpha = 0f;

    // Day state constants remain the same
    public final int day = 0;
    public final int dusk = 1;
    public final int night = 2;
    public final int dawn = 3;
    public int dayState = day;

    public Lighting(GamePanel gp) {
        this.gp = gp;
        // The constructor now directly calls the JavaFX-compatible method.
        setLightSource();
    }

    public void setLightSource() {
        // CHANGED: To draw our darkness filter, we'll use a temporary Canvas and its GraphicsContext.
        Canvas canvas = new Canvas(gp.screenWidth, gp.screenHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        if (gp.player.currentLight == null) {
            // Use JavaFX Color for a dark overlay
            gc.setFill(Color.rgb(0, 0, 20, 0.97));
        } else {
            // Get the center x and y of the light circle
            int centerX = gp.player.screenX + (gp.tileSize) / 2;
            int centerY = gp.player.screenY + (gp.tileSize) / 2;

            // CHANGED: Create a list of Stop objects for the JavaFX RadialGradient.
            // A Stop combines a color and its position (offset) in the gradient.
            List<Stop> stops = new ArrayList<>();
            stops.add(new Stop(0.0f, Color.rgb(0, 0, 20, 0.1f)));
            stops.add(new Stop(0.4f, Color.rgb(0, 0, 20, 0.42f)));
            stops.add(new Stop(0.5f, Color.rgb(0, 0, 20, 0.52f)));
            stops.add(new Stop(0.6f, Color.rgb(0, 0, 20, 0.61f)));
            stops.add(new Stop(0.65f, Color.rgb(0, 0, 20, 0.69f)));
            stops.add(new Stop(0.7f, Color.rgb(0, 0, 20, 0.76f)));
            stops.add(new Stop(0.75f, Color.rgb(0, 0, 20, 0.82f)));
            stops.add(new Stop(0.8f, Color.rgb(0, 0, 20, 0.87f)));
            stops.add(new Stop(0.85f, Color.rgb(0, 0, 20, 0.91f)));
            stops.add(new Stop(0.9f, Color.rgb(0, 0, 20, 0.92f)));
            stops.add(new Stop(0.95f, Color.rgb(0, 0, 20, 0.93f)));
            stops.add(new Stop(1.0f, Color.rgb(0, 0, 20, 0.94f)));

            // CHANGED: Create a JavaFX RadialGradient.
            RadialGradient gPaint = new RadialGradient(
                    0,                      // focusAngle
                    0,                      // focusDistance
                    centerX,                // centerX
                    centerY,                // centerY
                    gp.player.currentLight.lightRadius, // radius
                    false,                  // proportional
                    CycleMethod.NO_CYCLE,   // cycleMethod
                    stops                   // stops
            );

            // Set the gradient as the fill for the GraphicsContext
            gc.setFill(gPaint);
        }

        // Draw the darkness overlay covering the entire screen
        gc.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // CHANGED: Take a snapshot of the canvas to create the WritableImage.
        // This is more efficient than redrawing the gradient every frame.
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        darknessFilter = canvas.snapshot(params, null);
    }

    public void resetDay() {
        dayState = day;
        filterAlpha = 0f;
    }

    public void update() {
        // This method contains only game logic, so no changes are needed.
        if (gp.player.lightUpdated) {
            setLightSource();
            gp.player.lightUpdated = false;
        }

        // Check the state of the day
        if (dayState == day) {
            dayCounter++;
            if (dayCounter > 3600) { // 1 min day
                dayState = dusk;
                dayCounter = 0;
            }
        }
        if (dayState == dusk) {
            filterAlpha += 0.0005f;   // takes ~33 seconds
            if (filterAlpha > 1f) {
                filterAlpha = 1f;
                dayState = night;
            }
        }
        if (dayState == night) {
            dayCounter++;
            if (dayCounter > 3600) { // 1 min night
                dayState = dawn;
                dayCounter = 0;
            }
        }
        if (dayState == dawn) {
            filterAlpha -= 0.0005f;   // takes ~33 seconds
            if (filterAlpha < 0) {
                filterAlpha = 0;
                dayState = day;
            }
        }
    }

    // CHANGED: The draw method now accepts a JavaFX GraphicsContext.
    public void draw(GraphicsContext gc) {

        // Apply a global alpha for the fade-in/fade-out effect during dusk and dawn
        if (gp.currentArea == gp.outside) {
            gc.setGlobalAlpha(filterAlpha);
        }

        // Draw the pre-rendered darkness/light-source image
        if (gp.currentArea == gp.outside || gp.currentArea == gp.dungeon) {
            gc.drawImage(darknessFilter, 0, 0);
        }

        // Reset the global alpha to 1.0 so other game elements are drawn normally
        gc.setGlobalAlpha(1.0);

        // --- DEBUG TEXT ---
        String situation = "";
        switch (dayState) {
            case day: situation = "Day"; break;
            case dusk: situation = "Dusk"; break;
            case night: situation = "Night"; break;
            case dawn: situation = "Dawn"; break;
        }
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(50)); // Simplified font creation in JavaFX
        gc.fillText(situation, 800, 500); // Use fillText to draw strings
    }
}