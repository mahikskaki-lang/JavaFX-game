package sample.demo2.entity;

import sample.demo2.main.GamePanel;

/**
 * A simple visual representation for other players in a multiplayer session.
 * It uses the default Entity.draw() method to be drawn at its world coordinates,
 * not in the center of the screen like the main Player class.
 */
public class RemotePlayer extends Entity {

    public RemotePlayer(GamePanel gp) {
        super(gp);
        direction = "down";
        speed = 2; // Match your player's default speed
        getImage();
    }

    // Loads the standard player character sprites
    public void getImage() {
        up1 = setup("/player/boy_up_1", gp.tileSize, gp.tileSize);
        up2 = setup("/player/boy_up_2", gp.tileSize, gp.tileSize);
        down1 = setup("/player/boy_down_1", gp.tileSize, gp.tileSize);
        down2 = setup("/player/boy_down_2", gp.tileSize, gp.tileSize);
        left1 = setup("/player/boy_left_1", gp.tileSize, gp.tileSize);
        left2 = setup("/player/boy_left_2", gp.tileSize, gp.tileSize);
        right1 = setup("/player/boy_right_1", gp.tileSize, gp.tileSize);
        right2 = setup("/player/boy_right_2", gp.tileSize, gp.tileSize);
    }
}