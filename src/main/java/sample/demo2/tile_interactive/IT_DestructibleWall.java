package sample.demo2.tile_interactive;

import javafx.scene.paint.Color; // Changed to the correct JavaFX import
import sample.demo2.entity.Entity;
import sample.demo2.main.GamePanel;

// Removed: import java.awt.*;

public class IT_DestructibleWall extends InteractiveTile {

    GamePanel gp;

    public IT_DestructibleWall(GamePanel gp, int col, int row) {
        super(gp, col, row);
        this.gp = gp;

        this.worldX = gp.tileSize * col;
        this.worldY = gp.tileSize * row;

        down1 = setup("/tiles_interactive/destructiblewall", gp.tileSize, gp.tileSize);
        destructible = true;
        life = 3;
    }

    public boolean isCorrectItem(Entity entity) {
        boolean isCorrectItem = false;
        if (entity.currentWeapon.type == type_pickaxe) {
            isCorrectItem = true;
        }
        return isCorrectItem;
    }

    public void playSE() {
        gp.playSE(20);
    }

    public InteractiveTile getDestroyedForm() {
        InteractiveTile tile = null;
        return tile;
    }

    // This method is now corrected to return a JavaFX Color
    public Color getParticleColor() {
        // Use the Color.rgb() method to create a JavaFX Color from integer values
        Color color = Color.rgb(65, 65, 65);
        return color;
    }

    public int getParticleSize() {
        int size = 6; // pixels
        return size;
    }

    public int getParticleSpeed() {
        int speed = 1;
        return speed;
    }

    public int getParticleMaxLife() {
        int maxLife = 20;
        return maxLife;
    }
}