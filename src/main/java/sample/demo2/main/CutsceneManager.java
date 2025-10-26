package sample.demo2.main;

import sample.demo2.entity.PlayerDummy;
import sample.demo2.monster.MON_SkeletonLord;
import sample.demo2.object.OBJ_BlueHeart;
import sample.demo2.object.OBJ_Door_Iron;

// CHANGED: Imports for JavaFX
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CutsceneManager {

    GamePanel gp;
    // REMOVED: Graphics2D g2;
    public int sceneNum;
    public int scenePhase;

    int counter = 0;
    float alpha = 0f;
    int y;
    String endCredit;

    //Scene Number
    public final int NA = 0;
    public final int skeletonLord = 1;
    public final int ending = 2;


    public CutsceneManager(GamePanel gp) {
        this.gp = gp;
        endCredit = "Developed by\n"
                + "Mahim And Ritu"
                + "\n\n\n\n\n\n\n\n\n\n\n"
                + "Special Thanks\n\n"
                + "Arnab Bhattacharja\n"

                + "\n\n\n\n\n\n\n"
                + "Thank you for playing!";
    }

    // CHANGED: Method signature and calls
    public void draw(GraphicsContext gc) {
        switch(sceneNum) {
            case skeletonLord: scene_skeletonLord(gc); break;
            case ending: scene_ending(gc); break;
        }
    }

    // CHANGED: Method signature and calls
    public void scene_skeletonLord(GraphicsContext gc) {
        if(scenePhase == 0) {
            gp.bossBattleOn = true;
            for(int i = 0; i < gp.obj[1].length; i++) {
                if(gp.obj[gp.currentMap][i] == null) {
                    gp.obj[gp.currentMap][i] = new OBJ_Door_Iron(gp);
                    gp.obj[gp.currentMap][i].worldX = gp.tileSize * 25;
                    gp.obj[gp.currentMap][i].worldY = gp.tileSize * 28;
                    gp.obj[gp.currentMap][i].temp = true;
                    gp.playSE(21);
                    break;
                }
            }
            for(int i = 0; i < gp.npc[1].length; i++) {
                if(gp.npc[gp.currentMap][i] == null) {
                    gp.npc[gp.currentMap][i] = new PlayerDummy(gp);
                    gp.npc[gp.currentMap][i].worldX = gp.player.worldX;
                    gp.npc[gp.currentMap][i].worldY = gp.player.worldY;
                    gp.npc[gp.currentMap][i].direction = gp.player.direction;
                    break;
                }
            }
            gp.player.drawing = false;
            scenePhase++;
        }
        if(scenePhase == 1) {
            gp.player.worldY -= 2;
            if(gp.player.worldY < gp.tileSize * 16) {
                scenePhase++;
            }
        }
        if(scenePhase == 2) {
            for(int i = 0; i < gp.monster[1].length; i++) {
                if(gp.monster[gp.currentMap][i] != null && gp.monster[gp.currentMap][i].name.equals(MON_SkeletonLord.monName)) {
                    gp.monster[gp.currentMap][i].sleep = false;
                    gp.ui.npc = gp.monster[gp.currentMap][i];
                    scenePhase++;
                    break;
                }
            }
        }
        if(scenePhase == 3) {
            gp.ui.drawDialogueScreen(gc); // Pass gc
        }
        if(scenePhase == 4) {
            for(int i = 0; i < gp.npc[1].length; i++) {
                if(gp.npc[gp.currentMap][i] != null && gp.npc[gp.currentMap][i].name.equals(PlayerDummy.npcName)) {
                    gp.player.worldX = gp.npc[gp.currentMap][i].worldX;
                    gp.player.worldY = gp.npc[gp.currentMap][i].worldY;
                    gp.player.direction = gp.npc[gp.currentMap][i].direction;
                    gp.npc[gp.currentMap][i] = null;
                    break;
                }
            }
            gp.player.drawing = true;
            sceneNum = NA;
            scenePhase = 0;
            gp.gameState = gp.playState;
            gp.stopMusic();
            gp.playMusic(22);
        }
    }

    // CHANGED: Method signature and calls
    public void scene_ending(GraphicsContext gc) {
        if(scenePhase == 0) {
            gp.stopMusic();
            gp.ui.npc = new OBJ_BlueHeart(gp);
            scenePhase++;
        }
        if(scenePhase == 1) {
            gp.ui.drawDialogueScreen(gc); // Pass gc
        }
        if(scenePhase == 2) {
            gp.playSE(4);
            scenePhase++;
        }
        if(scenePhase == 3) {
            if(counterReached(300)) scenePhase++;
        }
        if(scenePhase == 4) {
            alpha = graduallyAlpha(alpha, 0.005f);
            drawBlackBackground(gc, alpha); // Pass gc
            if(alpha == 1f) {
                alpha = 0;
                scenePhase++;
            }
        }
        if(scenePhase == 5) {
            drawBlackBackground(gc, 1f); // Pass gc
            alpha = graduallyAlpha(alpha, 0.005f);
            String text = "After the fierce battle with the Skeleton Lord,\n"
                    + "the Blue Boy finally found the legendary treasure.\n"
                    + "But this is not the end of his journey.\n"
                    + "The Blue Boy's adventure has just begun.";
            drawString(gc, alpha, 38f, 200, text, 70); // Pass gc
            if(counterReached(600) && alpha == 1f) {
                gp.playMusic(0);
                alpha = 0;
                scenePhase++;
            }
        }
        if(scenePhase == 6) {
            drawBlackBackground(gc, 1f); // Pass gc
            alpha = graduallyAlpha(alpha, 0.01f);
            drawString(gc, alpha, 120f, gp.screenHeight/2, "Blue Boy Adventure", 40); // Pass gc
            if(counterReached(480) && alpha == 1f) {
                scenePhase++;
                alpha = 0;
            }
        }
        if(scenePhase == 7) {
            drawBlackBackground(gc, 1f); // Pass gc
            alpha = graduallyAlpha(alpha, 0.01f);
            y = gp.screenHeight/2;
            drawString(gc, alpha, 38f,  y, endCredit, 40); // Pass gc
            if(counterReached(240) && alpha == 1f) {
                scenePhase++;
                alpha = 0;
            }
        }
        if(scenePhase == 8) {
            drawBlackBackground(gc, 1f); // Pass gc
            y--;
            drawString(gc, 1f, 38f,  y, endCredit, 40); // Pass gc
            if(counterReached(1320)) {
                sceneNum = NA;
                scenePhase = 0;
                gp.gameState = gp.playState;
                gp.resetGame(false);
            }
        }
    }

    public boolean counterReached(int target) {
        boolean counterReached = false;
        counter++;
        if(counter > target) {
            counterReached = true;
            counter = 0;
        }
        return counterReached;
    }

    // CHANGED: Method signature and implementation
    public void drawBlackBackground(GraphicsContext gc, float alpha) {
        double originalAlpha = gc.getGlobalAlpha();
        gc.setGlobalAlpha(alpha);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        gc.setGlobalAlpha(originalAlpha);
    }

    // CHANGED: Method signature and implementation
    public void drawString(GraphicsContext gc, float alpha, float fontSize, int y, String text, int lineHeight) {
        double originalAlpha = gc.getGlobalAlpha();
        gc.setGlobalAlpha(alpha);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(gp.ui.maruMonica.getFamily(), fontSize));

        for(String line: text.split("\n")) {
            int x = gp.ui.getXforCenteredText(gc, line); // UI helper now needs gc
            gc.fillText(line, x, y);
            y += lineHeight;
        }
        gc.setGlobalAlpha(originalAlpha);
    }

    public float graduallyAlpha(float alpha, float grade) {
        alpha += grade;
        if(alpha > 1f) {
            alpha = 1f;
        }
        return alpha;
    }
}