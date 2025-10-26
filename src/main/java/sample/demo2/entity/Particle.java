package sample.demo2.entity;

import sample.demo2.main.GamePanel;

// CHANGED: Imports for JavaFX graphics
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Particle extends Entity{

    Entity generator;
    Color color; // CHANGED: This is now a JavaFX Color
    int size;
    int xd;
    int yd;

    // CHANGED: The 'color' parameter is now a JavaFX Color
    public Particle(GamePanel gp, Entity generator, Color color, int size, int speed, int maxLife, int xd, int yd) {
        super(gp);

        this.generator = generator;
        this.color = color;
        this.size = size;
        this.speed = speed;
        this.maxLife = maxLife;
        this.xd = xd;
        this.yd = yd;

        life = maxLife;
        int offset = (gp.tileSize/2) - size/2; //for center of generator
        worldX = generator.worldX + offset;
        worldY = generator.worldY + offset;
    }

    public void update()
    {
        life--;

        // If particle's life 1/3 of its maxLife or less, it adds 1 to yd, this yd value gets greater every loop. so, particle will go down.
        if(life < maxLife/3)
        {
            yd++;
            if (size > 0) { // Prevent size from becoming negative
                size--;
            }
        }

        worldX += xd * speed;
        worldY += yd * speed;

        if(life == 0)
        {
            alive = false;
        }
    }

    // CHANGED: Method now uses GraphicsContext instead of Graphics2D
    public void draw(GraphicsContext gc)
    {
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        // CHANGED: Set the fill color and draw a rectangle on the canvas
        gc.setFill(color);
        gc.fillRect(screenX, screenY, size, size);
    }
}