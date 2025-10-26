package sample.demo2.tile;

import sample.demo2.main.GamePanel;

// CHANGED: Imports for JavaFX
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Map extends TileManager{

    GamePanel gp;
    Image worldMap[]; // CHANGED: From BufferedImage to Image
    public boolean miniMapOn = false;

    public Map(GamePanel gp) {
        super(gp);
        this.gp = gp;
        createWorldMap();
    }

    // CHANGED: This method now uses a Canvas to create the map images
    public void createWorldMap() {
        worldMap = new Image[gp.maxMap];
        int worldMapWidth = gp.tileSize * gp.maxWorldCol;
        int worldMapHeight = gp.tileSize * gp.maxWorldRow;

        for(int i = 0; i < gp.maxMap; i++) {
            // Use a temporary Canvas to draw the map off-screen
            Canvas canvas = new Canvas(worldMapWidth, worldMapHeight);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            int col = 0;
            int row = 0;
            while(col < gp.maxWorldCol && row < gp.maxWorldRow) {
                int tileNum = mapTileNum[i][col][row];
                int x = gp.tileSize * col;
                int y = gp.tileSize * row;
                gc.drawImage(tile[tileNum].image, x, y); // tile[].image is already a JavaFX Image
                col++;
                if(col == gp.maxWorldCol) {
                    col = 0;
                    row++;
                }
            }
            // Take a snapshot of the canvas and store it as an Image
            worldMap[i] = canvas.snapshot(null, null);
        }
    }

    // CHANGED: Method now uses GraphicsContext
    public void drawFullMapScreen(GraphicsContext gc) {
        // Background Color
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Draw map
        int width = 500;
        int height = 500;
        int x = gp.screenWidth/2 - width/2;
        int y = gp.screenHeight/2 - height/2;
        gc.drawImage(worldMap[gp.currentMap], x, y, width, height);

        // Draw Player
        double scale = (double) (gp.tileSize * gp.maxWorldCol)/width;
        int playerX = (int)(x + gp.player.worldX/scale);
        int playerY = (int)(y + gp.player.worldY/scale);
        int playerSize = (int)(gp.tileSize/scale);
        gc.drawImage(gp.player.down1, playerX, playerY, playerSize, playerSize);

        // Hint
        gc.setFont(Font.font(gp.ui.maruMonica.getFamily(), 32));
        gc.setFill(Color.WHITE);
        gc.fillText("Press M to close", 750, 550);
    }

    // CHANGED: Method now uses GraphicsContext and its alpha settings
    public void drawMiniMap(GraphicsContext gc) {
        if(miniMapOn) {
            // Draw map
            int width = 200;
            int height = 200;
            int x = gp.screenWidth - width - 50;
            int y = 50;

            // Save original alpha, set new alpha, draw, then restore
            double originalAlpha = gc.getGlobalAlpha();
            gc.setGlobalAlpha(0.8);
            gc.drawImage(worldMap[gp.currentMap], x, y, width, height);

            // Draw Player
            double scale = (double) (gp.tileSize * gp.maxWorldCol)/width;
            int playerX = (int)(x + gp.player.worldX/scale);
            int playerY = (int)(y + gp.player.worldY/scale);
            int playerSize = (int)(gp.tileSize/3);
            gc.drawImage(gp.player.down1, playerX - 6, playerY - 6, playerSize, playerSize);

            // Restore alpha
            gc.setGlobalAlpha(originalAlpha);
        }
    }
}