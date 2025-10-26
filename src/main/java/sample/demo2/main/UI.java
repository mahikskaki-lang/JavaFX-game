package sample.demo2.main;

// CHANGED: Imports for JavaFX
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
//import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import sample.demo2.entity.Entity;
import sample.demo2.object.OBJ_Coin_Bronze;
import sample.demo2.object.OBJ_Heart;
import sample.demo2.object.OBJ_ManaCrystal;

import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UI {

    GamePanel gp;
    public Font maruMonica, purisaB;
    Image heart_full, heart_half, heart_blank, crystal_full, crystal_blank, coin;
    public boolean messageOn = false;
    ArrayList<String> message = new ArrayList<>();
    ArrayList<Integer> messageCounter = new ArrayList<>();
    public boolean gameFinished = false;
    public String currentDialogue = "";
    public int commandNum = 0;
    public int titleScreenState = 0;
    public int playerSlotCol = 0;
    public int playerSlotRow = 0;
    public int npcSlotCol = 0;
    public int npcSlotRow = 0;
    public Image titleScreenBackground;
    int subState = 0;
    int counter = 0;
    public Entity npc;
    int charIndex = 0;
    String combinedText = "";
    public Rectangle chatNotificationArea;

    // --- NEW FOR CHAT ---
    public List<String> chatLog = new ArrayList<>();
    public String currentChat = "";

    public int unreadMessages = 0;
    private Image chatNotificationIcon;

    public UI(GamePanel gp) {
        this.gp = gp;
        // ... (constructor remains the same) ...
        try (InputStream is1 = getClass().getResourceAsStream("/font/x12y16pxMaruMonica.ttf");
             InputStream is2 = getClass().getResourceAsStream("/font/Purisa Bold.ttf")) {
            maruMonica = Font.loadFont(is1, 40);
            purisaB = Font.loadFont(is2, 40);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (InputStream is = getClass().getResourceAsStream("/monster/zombie_background.png")) {
            if (is != null) {
                titleScreenBackground = new Image(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Entity heart = new OBJ_Heart(gp);
        heart_full = heart.image;
        heart_half = heart.image2;
        heart_blank = heart.image3;
        Entity crystal = new OBJ_ManaCrystal(gp);
        crystal_full = crystal.image;
        crystal_blank = crystal.image2;
        Entity bronzeCoin = new OBJ_Coin_Bronze(gp);
        coin = bronzeCoin.down1;
        chatNotificationArea = new Rectangle();

        try {
            chatNotificationIcon = new Image(getClass().getResourceAsStream("/objects/chat_notification.png"));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load chat notification icon!");
        }
    }

    public void addMessage(String text) {
        message.add(text);
        messageCounter.add(0);
    }

    // --- NEW: Method to add messages to the persistent chat log ---
    public void addChatMessage(String text) {
        chatLog.add(text);
        if (chatLog.size() > 7) { // Keep the last 7 messages
            chatLog.remove(0);
        }
    }

    // In UI.java

    public void draw(GraphicsContext gc) {
        gc.setFont(maruMonica);
        gc.setFill(Color.WHITE);

        // TITLE STATE
        if (gp.gameState == gp.titleState) {
            drawTitleScreen(gc);
        }
        // OTHERS
        else {
            if (gp.gameState == gp.playState) {
                drawPlayerLife(gc);
                drawMonsterLife(gc);
                drawMessage(gc);
            }
            if (gp.gameState == gp.pauseState) {
                drawPlayerLife(gc);
                drawPauseScreen(gc);
            }
            if (gp.gameState == gp.dialogueState) {
                drawDialogueScreen(gc);
            }
            if (gp.gameState == gp.characterState) {
                drawCharacterScreen(gc);
                drawInventory(gc, gp.player, true);
            }
            if (gp.gameState == gp.optionsState) {
                drawOptionsScreen(gc);
            }
            if (gp.gameState == gp.gameOverState) {
                drawGameOverScreen(gc);
            }
            if (gp.gameState == gp.transitionState) {
                drawTransition(gc);
            }
            if (gp.gameState == gp.tradeState) {
                drawTradeScreen(gc);
            }
            if (gp.gameState == gp.sleepState) {
                drawSleepScreen(gc);
            }
            // --- NEW: CHAT STATE ---
            if (gp.gameState == gp.chatState) {
                // Draw the essential game UI in the background
                drawPlayerLife(gc);
                // Draw the full-screen chat interface on top
                drawChatScreen(gc);
            }
            if (gp.gameState == gp.playState) {
                drawPlayerLife(gc);
                drawMonsterLife(gc);
                drawMessage(gc);
                drawChatNotification(gc); // --- NEW --- Call the draw method
            }
        }
    }

    // --- NEW: Method to draw the chat log ---
    // In UI.java

    // You can remove your old drawChatLog and drawChatInput methods.
// Replace them with this one:
    public void drawChatScreen(GraphicsContext gc) {
        // 1. DRAW MAIN CHAT WINDOW
        int x = gp.tileSize * 2;
        int y = gp.tileSize;
        int width = gp.screenWidth - (gp.tileSize * 4);
        int height = gp.screenHeight - (gp.tileSize * 2);
        drawSubWindow(gc, x, y, width, height);

        // 2. DRAW TITLE
        gc.setFont(Font.font(purisaB.getFamily(), 32));
        gc.setFill(Color.WHITE);
        String title = "Global Chat";
        int titleX = getXforCenteredText(gc, title);
        gc.fillText(title, titleX, y + gp.tileSize);

        // 3. DRAW MESSAGE LOG WITH BUBBLES
        gc.setFont(Font.font("Arial", 20));
        int messageX = x + gp.tileSize / 2;
        int messageY = y + gp.tileSize + 40;
        int lineHeight = 30;
        int padding = 10;

        for (String text : chatLog) {
            // Calculate text dimensions to draw the bubble around it
            Text tempText = new Text(text);
            tempText.setFont(gc.getFont());
            double textWidth = tempText.getLayoutBounds().getWidth();
            double textHeight = tempText.getLayoutBounds().getHeight();

            // Draw the message bubble
            gc.setFill(Color.rgb(255, 255, 255, 0.2)); // Semi-transparent white bubble
            gc.fillRoundRect(messageX, messageY - textHeight, textWidth + (padding * 2), textHeight + padding, 15, 15);

            // Draw the text
            gc.setFill(Color.WHITE);
            gc.fillText(text, messageX + padding, messageY);

            messageY += lineHeight;
        }

        // 4. DRAW INPUT FIELD AND SEND BUTTON
        int inputY = y + height - (int)(gp.tileSize * 1.5);
        int inputHeight = gp.tileSize;
        int sendButtonWidth = 80;

        // Draw input field background
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(messageX, inputY, width - sendButtonWidth - 40, inputHeight, 20, 20);

        // Draw the text being typed
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 24));
        String cursor = (System.currentTimeMillis() % 1000 > 500) ? "_" : "";
        gc.fillText(currentChat + cursor, messageX + padding, inputY + 30);

        // Draw "Send" button
        int sendButtonX = messageX + width - sendButtonWidth - 30;
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRoundRect(sendButtonX, inputY, sendButtonWidth, inputHeight, 20, 20);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 28));
        gc.fillText("Send", sendButtonX + 10, inputY + 32);
    }


    // ... (All other draw methods like drawPlayerLife, drawTitleScreen, etc., remain unchanged) ...
    public void drawPlayerLife(GraphicsContext gc) {
        int x = gp.tileSize / 2;
        int y = gp.tileSize / 2;
        int i = 0;
        int iconSize = 32;
        int manaStartX = (gp.tileSize / 2) - 5;
        int manaStartY = 0;

        // DRAW MAX LIFE
        while (i < gp.player.maxLife / 2) {
            gc.drawImage(heart_blank, x, y, iconSize, iconSize);
            i++;
            x += iconSize;
            manaStartY = y + 32;
            if (i % 8 == 0) { x = gp.tileSize / 2; y += iconSize; }
        }
        x = gp.tileSize / 2; y = gp.tileSize / 2; i = 0;

        // DRAW CURRENT LIFE
        while (i < gp.player.life) {
            gc.drawImage(heart_half, x, y, iconSize, iconSize);
            i++;
            if (i < gp.player.life) {
                gc.drawImage(heart_full, x, y, iconSize, iconSize);
            }
            i++;
            x += iconSize;
            if (i % 16 == 0) { x = gp.tileSize / 2; y += iconSize; }
        }
        x = manaStartX; y = manaStartY; i = 0;

        // DRAW MAX MANA
        while (i < gp.player.maxMana) {
            gc.drawImage(crystal_blank, x, y, iconSize, iconSize);
            i++; x += 20;
            if (i % 10 == 0) { x = manaStartX; y += iconSize; }
        }
        x = manaStartX; y = manaStartY; i = 0;

        // DRAW MANA
        while (i < gp.player.mana) {
            gc.drawImage(crystal_full, x, y, iconSize, iconSize);
            i++; x += 20;
            if (i % 10 == 0) { x = manaStartX; y += iconSize; }
        }
    }

    public void drawMonsterLife(GraphicsContext gc) {
        for (int i = 0; i < gp.monster[1].length; i++) {
            Entity monster = gp.monster[gp.currentMap][i];
            if (monster != null && monster.inCamera()) {
                if (monster.hpBarOn && !monster.boss) {
                    double oneScale = (double) gp.tileSize / monster.maxLife;
                    double hpBarValue = oneScale * monster.life;
                    if (hpBarValue < 0) hpBarValue = 0;
                    gc.setFill(Color.rgb(35, 35, 35));
                    gc.fillRect(monster.getScreenX() - 1, monster.getScreenY() - 16, gp.tileSize + 2, 12);
                    gc.setFill(Color.rgb(255, 0, 30));
                    gc.fillRect(monster.getScreenX(), monster.getScreenY() - 15, (int) hpBarValue, 10);
                    monster.hpBarCounter++;
                    if (monster.hpBarCounter > 600) {
                        monster.hpBarCounter = 0;
                        monster.hpBarOn = false;
                    }
                } else if (monster.boss) {
                    double oneScale = (double) gp.tileSize * 8 / monster.maxLife;
                    double hpBarValue = oneScale * monster.life;
                    int x = gp.screenWidth / 2 - gp.tileSize * 4;
                    int y = gp.tileSize * 10;
                    if (hpBarValue < 0) hpBarValue = 0;
                    gc.setFill(Color.rgb(35, 35, 35));
                    gc.fillRect(x - 1, y - 1, gp.tileSize * 8 + 2, 22);
                    gc.setFill(Color.rgb(255, 0, 30));
                    gc.fillRect(x, y, (int) hpBarValue, 20);
                    gc.setFont(Font.font(purisaB.getFamily(), 24));
                    gc.setFill(Color.WHITE);
                    gc.fillText(monster.name, x + 4, y - 10);
                }
            }
        }
    }

    public void drawMessage(GraphicsContext gc) {
        int messageX = gp.tileSize;
        int messageY = gp.tileSize * 4;
        gc.setFont(Font.font(maruMonica.getFamily(), 24));
        for (int i = 0; i < message.size(); i++) {
            if (message.get(i) != null) {
                gc.setFill(Color.BLACK);
                gc.fillText(message.get(i), messageX + 2, messageY + 2);
                gc.setFill(Color.WHITE);
                gc.fillText(message.get(i), messageX, messageY);
                int counter = messageCounter.get(i) + 1;
                messageCounter.set(i, counter);
                messageY += 50;
                if (messageCounter.get(i) > 150) {
                    message.remove(i);
                    messageCounter.remove(i);
                }
            }
        }
    }


    public void receiveChatMessage(String text) {
        // Add the message to the log
        chatLog.add(text);
        if (chatLog.size() > 7) {
            chatLog.remove(0);
        }

        // --- NEW --- If the chat screen is not currently open, this is an unread message.
        if (gp.gameState != gp.chatState) {
            unreadMessages++;
        }
    }


    // --- NEW, IMPROVED VERSION ---
    public void drawChatNotification(GraphicsContext gc) {
        // Only draw if there are unread messages and the icon has loaded successfully.
        if (unreadMessages > 0 && chatNotificationIcon != null) {

            // --- 1. Define positions and sizes ---
            int iconSize = gp.tileSize;
            int iconX = gp.screenWidth - iconSize - 10;
            int iconY = 10;
            chatNotificationArea.x = iconX;
            chatNotificationArea.y = iconY;
            chatNotificationArea.width = iconSize;
            chatNotificationArea.height = iconSize;
            // Draw the main chat icon
            gc.drawImage(chatNotificationIcon, iconX, iconY, iconSize, iconSize);

            // --- 2. Define the notification badge properties ---
            int badgeDiameter = 24;
            int badgeRadius = badgeDiameter / 2;
            // Position the badge's center on the top-right corner of the icon
            int badgeCenterX = iconX + iconSize - 5; // Overlap slightly
            int badgeCenterY = iconY + 5;            // Overlap slightly

            // --- 3. Draw the badge with a border for a cleaner look ---
            // Draw the white border by drawing a slightly larger circle first
            int borderSize = 2;
            gc.setFill(Color.WHITE);
            gc.fillOval(
                    badgeCenterX - badgeRadius - borderSize,
                    badgeCenterY - badgeRadius - borderSize,
                    badgeDiameter + (borderSize * 2),
                    badgeDiameter + (borderSize * 2)
            );

            // Draw the main red badge
            gc.setFill(Color.RED);
            gc.fillOval(
                    badgeCenterX - badgeRadius,
                    badgeCenterY - badgeRadius,
                    badgeDiameter,
                    badgeDiameter
            );

            // --- 4. Prepare and draw the unread count number ---
            String count = String.valueOf(unreadMessages);
            gc.setFont(Font.font("Arial BOLD", 16));
            gc.setFill(Color.WHITE);

            // Measure the text to accurately center it
            Text theText = new Text(count);
            theText.setFont(gc.getFont());
            double textWidth = theText.getLayoutBounds().getWidth();
            double textHeight = theText.getLayoutBounds().getHeight();

            // Calculate the exact position to draw the text so it's centered inside the badge
            double textX = badgeCenterX - (textWidth / 2.0);
            // The Y coordinate for fillText is the baseline, so we adjust slightly for visual centering
            double textY = badgeCenterY + (textHeight / 4.0);

            // Finally, draw the number
            gc.fillText(count, textX, textY);
        }else {
            // --- NEW --- If the icon is not drawn, reset the rectangle to an unclickable area.
            chatNotificationArea.x = 0;
            chatNotificationArea.y = 0;
            chatNotificationArea.width = 0;
            chatNotificationArea.height = 0;
        }
    }

    public void drawTitleScreen(GraphicsContext gc) {
        // Draw the background image first
        if (titleScreenBackground != null) {
            gc.drawImage(titleScreenBackground, 0, 0, gp.screenWidth, gp.screenHeight);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        }

        // --- Font and Color Setup ---
        Color textColor = Color.rgb(150, 0, 0); // Dark Bloody Red
        Color borderColor = Color.WHITE;
        Color shadowColor = Color.BLACK;

        // Main title screen menu (titleScreenState == 0)
        if (titleScreenState == 0) {
            // --- TITLE TEXT ---
            gc.setFont(Font.font(maruMonica.getFamily(), 96));
            String text = "All Of us are Dead";
            int x = getXforCenteredText(gc, text);
            int y = gp.tileSize * 3;

            // Draw shadow first
            gc.setFill(shadowColor);
            gc.fillText(text, x + 5, y + 5);

            // Draw white border
            gc.setStroke(borderColor);
            gc.setLineWidth(2);
            gc.strokeText(text, x, y);

            // Draw main red text
            gc.setFill(textColor);
            gc.fillText(text, x, y);

            // --- PLAYER IMAGE ---
//            x = gp.screenWidth / 2 - gp.tileSize;
//            y += gp.tileSize * 2;
//            gc.drawImage(gp.player.down1, x, y, gp.tileSize * 2, gp.tileSize * 2);

            // --- MENU OPTIONS ---
            gc.setFont(Font.font(maruMonica.getFamily(), 48));

            text = "NEW GAME";
            x = getXforCenteredText(gc, text);
            y += gp.tileSize * 3.5;
            gc.strokeText(text, x, y); // Border
            gc.fillText(text, x, y);   // Fill
            if (commandNum == 0) {
                gc.strokeText(">", x - gp.tileSize, y);
                gc.fillText(">", x - gp.tileSize, y);
            }

            text = "LOAD GAME";
            x = getXforCenteredText(gc, text);
            y += gp.tileSize;
            gc.strokeText(text, x, y); // Border
            gc.fillText(text, x, y);   // Fill
            if (commandNum == 1) {
                gc.strokeText(">", x - gp.tileSize, y);
                gc.fillText(">", x - gp.tileSize, y);
            }

            // --- NEW MULTIPLAYER OPTION ---
            text = "MULTIPLAYER";
            x = getXforCenteredText(gc, text);
            y += gp.tileSize;
            gc.strokeText(text, x, y); // Border
            gc.fillText(text, x, y);   // Fill
            if (commandNum == 2) {
                gc.strokeText(">", x - gp.tileSize, y);
                gc.fillText(">", x - gp.tileSize, y);
            }

            text = "QUIT";
            x = getXforCenteredText(gc, text);
            y += gp.tileSize;
            gc.strokeText(text, x, y); // Border
            gc.fillText(text, x, y);   // Fill
            if (commandNum == 3) { // <-- UPDATED from 2 to 3
                gc.strokeText(">", x - gp.tileSize, y);
                gc.fillText(">", x - gp.tileSize, y);
            }

            // Character selection screen (titleScreenState == 1)
        } else if (titleScreenState == 1) {
            gc.setFont(Font.font(maruMonica.getFamily(), 42));
            gc.setStroke(borderColor);
            gc.setFill(textColor);
            gc.setLineWidth(2);

            String text = "Game Type!!";
            int x = getXforCenteredText(gc, text);
            int y = gp.tileSize * 3;
            gc.strokeText(text, x, y);
            gc.fillText(text, x, y);

            text = "Fighter";
            x = getXforCenteredText(gc, text);
            y += gp.tileSize * 3;
            gc.strokeText(text, x, y);
            gc.fillText(text, x, y);
            if (commandNum == 0) {
                gc.strokeText(">", x - gp.tileSize, y);
                gc.fillText(">", x - gp.tileSize, y);
            }

            text = "Thief";
            x = getXforCenteredText(gc, text);
            y += gp.tileSize;
            gc.strokeText(text, x, y);
            gc.fillText(text, x, y);
            if (commandNum == 1) {
                gc.strokeText(">", x - gp.tileSize, y);
                gc.fillText(">", x - gp.tileSize, y);
            }

            text = "Sorcerer";
            x = getXforCenteredText(gc, text);
            y += gp.tileSize;
            gc.strokeText(text, x, y);
            gc.fillText(text, x, y);
            if (commandNum == 2) {
                gc.strokeText(">", x - gp.tileSize, y);
                gc.fillText(">", x - gp.tileSize, y);
            }

            text = "Back";
            x = getXforCenteredText(gc, text);
            y += gp.tileSize * 2;
            gc.strokeText(text, x, y);
            gc.fillText(text, x, y);
            if (commandNum == 3) {
                gc.strokeText(">", x - gp.tileSize, y);
                gc.fillText(">", x - gp.tileSize, y);
            }
        }else if (titleScreenState == 2) {
            gc.setFont(Font.font(maruMonica.getFamily(), 42));
            gc.setStroke(borderColor);
            gc.setFill(textColor);
            gc.setLineWidth(2);

            String text = "Multiplayer";
            int x = getXforCenteredText(gc, text);
            int y = gp.tileSize * 3;
            gc.strokeText(text, x, y);
            gc.fillText(text, x, y);

            text = "Host Game";
            x = getXforCenteredText(gc, text);
            y += gp.tileSize * 3;
            gc.strokeText(text, x, y);
            gc.fillText(text, x, y);
            if (commandNum == 0) {
                gc.strokeText(">", x - gp.tileSize, y);
                gc.fillText(">", x - gp.tileSize, y);
            }

            text = "Join Game";
            x = getXforCenteredText(gc, text);
            y += gp.tileSize;
            gc.strokeText(text, x, y);
            gc.fillText(text, x, y);
            if (commandNum == 1) {
                gc.strokeText(">", x - gp.tileSize, y);
                gc.fillText(">", x - gp.tileSize, y);
                // In a real game, you would draw a text box here to enter the IP address.
            }

            text = "Back";
            x = getXforCenteredText(gc, text);
            y += gp.tileSize * 2;
            gc.strokeText(text, x, y);
            gc.fillText(text, x, y);
            if (commandNum == 2) {
                gc.strokeText(">", x - gp.tileSize, y);
                gc.fillText(">", x - gp.tileSize, y);
            }
        }
    }
    public void drawPauseScreen(GraphicsContext gc) {
        gc.setFont(Font.font(maruMonica.getFamily(), 80));
        gc.setFill(Color.WHITE);
        String text = "GAME PAUSED";
        int x = getXforCenteredText(gc, text);
        int y = gp.screenHeight / 2;
        gc.fillText(text, x, y);
    }

    public void drawDialogueScreen(GraphicsContext gc) {
        int x = gp.tileSize * 3;
        int y = gp.tileSize / 2;
        int width = gp.screenWidth - (gp.tileSize * 6);
        int height = gp.tileSize * 4;
        drawSubWindow(gc, x, y, width, height);
        gc.setFont(Font.font(purisaB.getFamily(), 18));
        gc.setFill(Color.WHITE);
        x += gp.tileSize;
        y += gp.tileSize;
        if (npc.dialogues[npc.dialogueSet][npc.dialogueIndex] != null) {
            char[] characters = npc.dialogues[npc.dialogueSet][npc.dialogueIndex].toCharArray();
            if (charIndex < characters.length) {
                gp.playSE(17);
                String s = String.valueOf(characters[charIndex]);
                combinedText += s;
                currentDialogue = combinedText;
                charIndex++;
            }
            if (gp.keyH.enterPressed) {
                charIndex = 0;
                combinedText = "";
                if (gp.gameState == gp.dialogueState || gp.gameState == gp.cutsceneState) {
                    npc.dialogueIndex++;
                    gp.keyH.enterPressed = false;
                }
            }
        } else {
            npc.dialogueIndex = 0;
            if (gp.gameState == gp.dialogueState) {
                gp.gameState = gp.playState;
            }
            if (gp.gameState == gp.cutsceneState) {
                gp.csManager.scenePhase++;
            }
        }
        for (String line : currentDialogue.split("\n")) {
            gc.fillText(line, x, y);
            y += 40;
        }
    }

    public void drawCharacterScreen(GraphicsContext gc) {
        final int frameX = gp.tileSize * 2;
        final int frameY = gp.tileSize;
        final int frameWidth = gp.tileSize * 5;
        final int frameHeight = gp.tileSize * 10;
        drawSubWindow(gc, frameX, frameY, frameWidth, frameHeight);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(maruMonica.getFamily(), 32));
        int textX = frameX + 20;
        int textY = frameY + gp.tileSize;
        final int lineHeight = 35;

        gc.fillText("Level", textX, textY); textY += lineHeight;
        gc.fillText("Life", textX, textY); textY += lineHeight;
        gc.fillText("Mana", textX, textY); textY += lineHeight;
        gc.fillText("Strength", textX, textY); textY += lineHeight;
        gc.fillText("Dexterity", textX, textY); textY += lineHeight;
        gc.fillText("Attack", textX, textY); textY += lineHeight;
        gc.fillText("Defence", textX, textY); textY += lineHeight;
        gc.fillText("Exp", textX, textY); textY += lineHeight;
        gc.fillText("Next Level", textX, textY); textY += lineHeight;
        gc.fillText("Coin", textX, textY); textY += lineHeight + 10;
        gc.fillText("Weapon", textX, textY); textY += lineHeight + 15;
        gc.fillText("Shield", textX, textY);

        int tailX = (frameX + frameWidth) - 30;
        textY = frameY + gp.tileSize;
        String value;

        value = String.valueOf(gp.player.level);
        textX = getXforAlignToRight(gc, value, tailX);
        gc.fillText(value, textX, textY); textY += lineHeight;
        value = String.valueOf(gp.player.life + "/" + gp.player.maxLife);
        textX = getXforAlignToRight(gc, value, tailX);
        gc.fillText(value, textX, textY); textY += lineHeight;
        value = String.valueOf(gp.player.mana + "/" + gp.player.maxMana);
        textX = getXforAlignToRight(gc, value, tailX);
        gc.fillText(value, textX, textY); textY += lineHeight;
        value = String.valueOf(gp.player.strength);
        textX = getXforAlignToRight(gc, value, tailX);
        gc.fillText(value, textX, textY); textY += lineHeight;
        value = String.valueOf(gp.player.dexterity);
        textX = getXforAlignToRight(gc, value, tailX);
        gc.fillText(value, textX, textY); textY += lineHeight;
        value = String.valueOf(gp.player.attack);
        textX = getXforAlignToRight(gc, value, tailX);
        gc.fillText(value, textX, textY); textY += lineHeight;
        value = String.valueOf(gp.player.defense);
        textX = getXforAlignToRight(gc, value, tailX);
        gc.fillText(value, textX, textY); textY += lineHeight;
        value = String.valueOf(gp.player.exp);
        textX = getXforAlignToRight(gc, value, tailX);
        gc.fillText(value, textX, textY); textY += lineHeight;
        value = String.valueOf(gp.player.nextLevelExp);
        textX = getXforAlignToRight(gc, value, tailX);
        gc.fillText(value, textX, textY); textY += lineHeight;
        value = String.valueOf(gp.player.coin);
        textX = getXforAlignToRight(gc, value, tailX);
        gc.fillText(value, textX, textY); textY += lineHeight;
        gc.drawImage(gp.player.currentWeapon.down1, tailX - gp.tileSize + 5, textY - 24);
        textY += gp.tileSize;
        if(gp.player.currentShield!= null) {
            gc.drawImage(gp.player.currentShield.down1, tailX - gp.tileSize + 5, textY - 24);
        }
    }
    public void drawSubWindow(GraphicsContext gc, int x, int y, int width, int height) {
        gc.setFill(Color.rgb(0, 0, 0, 0.82)); // 210/255 = ~0.82 alpha
        gc.fillRoundRect(x, y, width, height, 35, 35);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(5);
        gc.strokeRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25);
    }
    public void drawInventory(GraphicsContext gc, Entity entity, boolean cursor) {
        int frameX = 0, frameY = 0, frameWidth = 0, frameHeight = 0, slotCol = 0, slotRow = 0;
        if (entity == gp.player) {
            frameX = gp.tileSize * 12;
            frameY = gp.tileSize;
            frameWidth = gp.tileSize * 6;
            frameHeight = gp.tileSize * 5;
            slotCol = playerSlotCol;
            slotRow = playerSlotRow;
        } else {
            frameX = gp.tileSize * 2;
            frameY = gp.tileSize;
            frameWidth = gp.tileSize * 6;
            frameHeight = gp.tileSize * 5;
            slotCol = npcSlotCol;
            slotRow = npcSlotRow;
        }
        drawSubWindow(gc, frameX, frameY, frameWidth, frameHeight);
        final int slotXstart = frameX + 20;
        final int slotYstart = frameY + 20;
        int slotX = slotXstart;
        int slotY = slotYstart;
        int slotSize = gp.tileSize + 3;
        for (int i = 0; i < entity.inventory.size(); i++) {
            if (entity.inventory.get(i) == entity.currentWeapon || entity.inventory.get(i) == entity.currentShield || entity.inventory.get(i) == entity.currentLight) {
                gc.setFill(Color.rgb(240, 190, 90));
                gc.fillRoundRect(slotX, slotY, gp.tileSize, gp.tileSize, 10, 10);
            }
            gc.drawImage(entity.inventory.get(i).down1, slotX, slotY);
            if (entity == gp.player && entity.inventory.get(i).amount > 1) {
                gc.setFont(Font.font(32));
                String s = "" + entity.inventory.get(i).amount;
                int amountX = getXforAlignToRight(gc, s, slotX + 44);
                int amountY = slotY + gp.tileSize;
                gc.setFill(Color.rgb(60, 60, 60));
                gc.fillText(s, amountX, amountY);
                gc.setFill(Color.WHITE);
                gc.fillText(s, amountX - 3, amountY - 3);
            }
            slotX += slotSize;
            if (i == 4 || i == 9 || i == 14) {
                slotX = slotXstart;
                slotY += slotSize;
            }
        }
        if (cursor) {
            int cursorX = slotXstart + (slotSize * slotCol);
            int cursorY = slotYstart + (slotSize * slotRow);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(3);
            gc.strokeRoundRect(cursorX, cursorY, gp.tileSize, gp.tileSize, 10, 10);
            int dFrameX = frameX;
            int dFrameY = frameY + frameHeight;
            int dFrameWidth = frameWidth;
            int dFrameHeight = gp.tileSize * 3;
            int textX = dFrameX + 20;
            int textY = dFrameY + gp.tileSize;
            gc.setFont(Font.font(maruMonica.getFamily(), 28));
            int itemIndex = getItemIndexOnSlot(slotCol, slotRow);
            if (itemIndex < entity.inventory.size()) {
                drawSubWindow(gc, dFrameX, dFrameY, dFrameWidth, dFrameHeight);
                for (String line : entity.inventory.get(itemIndex).description.split("\n")) {
                    gc.fillText(line, textX, textY);
                    textY += 32;
                }
            }
        }
    }

    public void drawGameOverScreen(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        gc.setFont(Font.font(maruMonica.getFamily(), 110));
        String text = "Game Over";
        gc.setFill(Color.BLACK);
        int x = getXforCenteredText(gc, text);
        int y = gp.tileSize * 4;
        gc.fillText(text, x, y);
        gc.setFill(Color.WHITE);
        gc.fillText(text, x - 4, y - 4);

        gc.setFont(Font.font(maruMonica.getFamily(), 50));
        text = "Retry";
        x = getXforCenteredText(gc, text);
        y += gp.tileSize * 4;
        gc.fillText(text, x, y);
        if (commandNum == 0) {
            gc.fillText(">", x - 40, y);
        }

        text = "Quit";
        x = getXforCenteredText(gc, text);
        y += 55;
        gc.fillText(text, x, y);
        if (commandNum == 1) {
            gc.fillText(">", x - 40, y);
        }
    }

    public void drawOptionsScreen(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(maruMonica.getFamily(), 32));
        int frameX = gp.tileSize * 6;
        int frameY = gp.tileSize;
        int frameWidth = gp.tileSize * 8;
        int frameHeight = gp.tileSize * 10;
        drawSubWindow(gc, frameX, frameY, frameWidth, frameHeight);

        switch (subState) {
            case 0: options_top(gc, frameX, frameY); break;
            case 1: options_fullScreenNotification(gc, frameX, frameY); break;
            case 2: options_control(gc, frameX, frameY); break;
            case 3: options_endGameConfirmation(gc, frameX, frameY); break;
        }
        gp.keyH.enterPressed = false;
    }

    public void options_top(GraphicsContext gc, int frameX, int frameY) {
        // Set text color for this menu
        gc.setFill(Color.WHITE);

        int textX;
        int textY;

        // TITLE
        String text = "Options";
        textX = getXforCenteredText(gc, text);
        textY = frameY + gp.tileSize;
        gc.fillText(text, textX, textY);

        // MENU ITEMS
        textX = frameX + gp.tileSize;
        textY += gp.tileSize * 2;
        gc.fillText("Full Screen", textX, textY);
        if (commandNum == 0) {
            gc.fillText(">", textX - 25, textY);
            if (gp.keyH.enterPressed) {
                gp.fullScreenOn = !gp.fullScreenOn;
                subState = 1;
            }
        }

        textY += gp.tileSize;
        gc.fillText("Music", textX, textY);
        if (commandNum == 1) gc.fillText(">", textX - 25, textY);

        textY += gp.tileSize;
        gc.fillText("SE", textX, textY);
        if (commandNum == 2) gc.fillText(">", textX - 25, textY);

        textY += gp.tileSize;
        gc.fillText("Controls", textX, textY);
        if (commandNum == 3) {
            gc.fillText(">", textX - 25, textY);
            if (gp.keyH.enterPressed) {
                subState = 2;
                commandNum = 0;
            }
        }

        textY += gp.tileSize;
        gc.fillText("End Game", textX, textY);
        if (commandNum == 4) {
            gc.fillText(">", textX - 25, textY);
            if (gp.keyH.enterPressed) {
                subState = 3;
                commandNum = 0;
            }
        }

        textY += gp.tileSize * 2;
        gc.fillText("Back", textX, textY);
        if (commandNum == 5) {
            gc.fillText(">", textX - 25, textY);
            if (gp.keyH.enterPressed) {
                gp.gameState = gp.playState;
                commandNum = 0;
            }
        }

        // --- INTERACTIVE ELEMENTS ---
        // Full Screen Check Box
        textX = frameX + (int)(gp.tileSize * 4.5);
        textY = frameY + gp.tileSize * 2 + 24;
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeRect(textX, textY, 24, 24);
        if (gp.fullScreenOn) gc.fillRect(textX, textY, 24, 24);

        // Music Volume
        textY += gp.tileSize;
        gc.strokeRect(textX, textY, 120, 24);
        int volumeWidth = 24 * gp.music.volumeScale;
        gc.fillRect(textX, textY, volumeWidth, 24);

        // SE Volume
        textY += gp.tileSize;
        gc.strokeRect(textX, textY, 120, 24);
        volumeWidth = 24 * gp.se.volumeScale;
        gc.fillRect(textX, textY, volumeWidth, 24);

        gp.config.saveConfig();
    }

    public void options_fullScreenNotification(GraphicsContext gc, int frameX, int frameY) {
        // Set text color for this screen
        gc.setFill(Color.WHITE);

        int textX = frameX + gp.tileSize;
        int textY = frameY + gp.tileSize * 3;

        currentDialogue = "The change will take \neffect after restarting \nthe game.";
        for(String line: currentDialogue.split("\n")) {
            gc.fillText(line, textX, textY);
            textY += 40;
        }

        // "Back" button
        textY = frameY + gp.tileSize * 9;
        gc.fillText("Back", textX, textY);
        if(commandNum == 0) {
            gc.fillText(">", textX - 25, textY);
            if(gp.keyH.enterPressed) {
                subState = 0;
            }
        }
    }

    public void options_control(GraphicsContext gc, int frameX, int frameY) {
        // Set text color for this screen
        gc.setFill(Color.WHITE);

        int textX;
        int textY;

        // --- TITLE ---
        String text = "Controls";
        textX = getXforCenteredText(gc, text);
        textY = frameY + gp.tileSize;
        gc.fillText(text, textX, textY);

        // --- ACTIONS ---
        textX = frameX + gp.tileSize;
        textY += gp.tileSize;
        gc.fillText("Move", textX, textY);
        textY += gp.tileSize;
        gc.fillText("Confirm/Attack", textX, textY);
        textY += gp.tileSize;
        gc.fillText("Shoot/Cast", textX, textY);
        textY += gp.tileSize;
        gc.fillText("Character Screen", textX, textY);
        textY += gp.tileSize;
        gc.fillText("Pause", textX, textY);
        textY += gp.tileSize;
        gc.fillText("Options", textX, textY);

        // --- KEYS ---
        textX = frameX + gp.tileSize * 6;
        textY = frameY + gp.tileSize * 2;
        gc.fillText("WASD", textX, textY);
        textY += gp.tileSize;
        gc.fillText("ENTER", textX, textY);
        textY += gp.tileSize;
        gc.fillText("F", textX, textY);
        textY += gp.tileSize;
        gc.fillText("C", textX, textY);
        textY += gp.tileSize;
        gc.fillText("P", textX, textY);
        textY += gp.tileSize;
        gc.fillText("ESC", textX, textY);

        // --- BACK BUTTON ---
        textX = frameX + gp.tileSize;
        textY = frameY + gp.tileSize * 9;
        gc.fillText("Back", textX, textY);
        if(commandNum == 0) {
            gc.fillText(">", textX - 25, textY);
            if(gp.keyH.enterPressed) {
                subState = 0;
                commandNum = 3; // Highlights the "Controls" option when returning
            }
        }
    }



    public void options_endGameConfirmation(GraphicsContext gc, int frameX, int frameY) {
        // Set text color for this screen
        gc.setFill(Color.WHITE);

        int textX = frameX + gp.tileSize;
        int textY = frameY + gp.tileSize * 3;

        currentDialogue = "Quit the game and \nreturn to the title screen?";
        for(String line: currentDialogue.split("\n")) {
            gc.fillText(line, textX, textY);
            textY += 40;
        }

        // --- YES Button ---
        String text = "Yes";
        textX = getXforCenteredText(gc, text);
        textY += gp.tileSize * 3;
        gc.fillText(text, textX, textY);
        if(commandNum == 0) {
            gc.fillText(">", textX - 25, textY);
            if(gp.keyH.enterPressed) {
                subState = 0;
                gp.ui.titleScreenState = 0;
                gp.gameState = gp.titleState;
                gp.resetGame(true);
                gp.stopMusic();
            }
        }

        // --- NO Button ---
        text = "No";
        textX = getXforCenteredText(gc, text);
        textY += gp.tileSize;
        gc.fillText(text, textX, textY);
        if(commandNum == 1) {
            gc.fillText(">", textX - 25, textY);
            if(gp.keyH.enterPressed) {
                subState = 0;
                commandNum = 4; // Highlights "End Game" when returning
            }
        }
    }

    public void drawTransition(GraphicsContext gc) {
        counter++;
        gc.setFill(Color.rgb(0, 0, 0, counter * 5 / 255.0));
        gc.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        if (counter == 50) {
            counter = 0;
            gp.gameState = gp.playState;
            gp.player.worldX = gp.tileSize * gp.eHandler.tempCol;
            gp.player.worldY = gp.tileSize * gp.eHandler.tempRow;
            gp.currentMap = gp.eHandler.tempMap;
            gp.eHandler.previousEventX = gp.player.worldX;
            gp.eHandler.previousEventY = gp.player.worldY;
            gp.changeArea();
        }
    }

    public void drawTradeScreen(GraphicsContext gc) {
        switch (subState) {
            case 0: trade_select(gc); break;
            case 1: trade_buy(gc); break;
            case 2: trade_sell(gc); break;
        }
        gp.keyH.enterPressed = false;
    }

    public void trade_select(GraphicsContext gc) {
        npc.dialogueSet = 0;
        drawDialogueScreen(gc);
        int x = gp.tileSize * 15;
        int y = gp.tileSize * 4;
        int width = gp.tileSize * 3;
        int height = (int)(gp.tileSize * 3.5);
        drawSubWindow(gc, x, y, width, height);
        x += gp.tileSize;
        y += gp.tileSize;
        gc.fillText("Buy", x, y);
        if(commandNum == 0) {
            gc.fillText(">", x-24, y);
            if(gp.keyH.enterPressed) subState = 1;
        }
        y += gp.tileSize;
        gc.fillText("Sell", x, y);
        if(commandNum == 1) {
            gc.fillText(">", x-24, y);
            if(gp.keyH.enterPressed) subState = 2;
        }
        y += gp.tileSize;
        gc.fillText("Leave", x, y);
        if(commandNum == 2) {
            gc.fillText(">", x-24, y);
            if(gp.keyH.enterPressed) {
                commandNum = 0;
                npc.startDialogue(npc, 1);
            }
        }
    }

    public void trade_buy(GraphicsContext gc) {
        drawInventory(gc, gp.player, false);
        drawInventory(gc, npc, true);
        int x = gp.tileSize * 2;
        int y = gp.tileSize * 9;
        int width = gp.tileSize * 6;
        int height = gp.tileSize * 2;
        drawSubWindow(gc, x, y, width, height);
        gc.fillText("[ESC] Back", x + 24, y + 60);
        x = gp.tileSize * 12;
        drawSubWindow(gc, x, y, width, height);
        gc.fillText("Your Coin: " + gp.player.coin, x + 24, y + 60);
        int itemIndex = getItemIndexOnSlot(npcSlotCol, npcSlotRow);
        if (itemIndex < npc.inventory.size()) {
            x = (int)(gp.tileSize * 5.5);
            y = (int)(gp.tileSize * 5.5);
            width = (int)(gp.tileSize * 2.5);
            height = gp.tileSize;
            drawSubWindow(gc, x, y, width, height);
            gc.drawImage(coin, x + 10, y + 8, 32, 32);
            int price = npc.inventory.get(itemIndex).price;
            String text = String.valueOf(price);
            x = getXforAlignToRight(gc, text, (gp.tileSize * 8) - 20);
            gc.fillText(text, x, y + 34);
            if (gp.keyH.enterPressed) {
                if (npc.inventory.get(itemIndex).price > gp.player.coin) {
                    subState = 0;
                    npc.startDialogue(npc, 2);
                } else {
                    if (gp.player.canObtainItem(npc.inventory.get(itemIndex))) {
                        gp.player.coin -= npc.inventory.get(itemIndex).price;
                    } else {
                        subState = 0;
                        npc.startDialogue(npc, 3);
                    }
                }
            }
        }
    }

    public void trade_sell(GraphicsContext gc) {
        drawInventory(gc, gp.player, true);
        int x, y, width, height;
        x = gp.tileSize * 2;
        y = gp.tileSize * 9;
        width = gp.tileSize * 6;
        height = gp.tileSize * 2;
        drawSubWindow(gc, x, y, width, height);
        gc.fillText("[ESC] Back", x + 24, y + 60);
        x = gp.tileSize * 12;
        drawSubWindow(gc, x, y, width, height);
        gc.fillText("Your Coin: " + gp.player.coin, x + 24, y + 60);
        int itemIndex = getItemIndexOnSlot(playerSlotCol, playerSlotRow);
        if (itemIndex < gp.player.inventory.size()) {
            x = (int)(gp.tileSize * 15.5);
            y = (int)(gp.tileSize * 5.5);
            width = (int)(gp.tileSize * 2.5);
            height = gp.tileSize;
            drawSubWindow(gc, x, y, width, height);
            gc.drawImage(coin, x + 10, y + 8, 32, 32);
            int price = gp.player.inventory.get(itemIndex).price / 2;
            String text = String.valueOf(price);
            x = getXforAlignToRight(gc, text, (gp.tileSize * 18) - 20);
            gc.fillText(text, x, y + 34);
            if (gp.keyH.enterPressed) {
                if (gp.player.inventory.get(itemIndex) == gp.player.currentWeapon || gp.player.inventory.get(itemIndex) == gp.player.currentShield) {
                    commandNum = 0;
                    subState = 0;
                    npc.startDialogue(npc, 4);
                } else {
                    if (gp.player.inventory.get(itemIndex).amount > 1) {
                        gp.player.inventory.get(itemIndex).amount--;
                    } else {
                        gp.player.inventory.remove(itemIndex);
                    }
                    gp.player.coin += price;
                }
            }
        }
    }

    public void drawSleepScreen(GraphicsContext gc) {
        counter++;
        if (counter < 120) {
            gp.eManager.lighting.filterAlpha += 0.01f;
            if (gp.eManager.lighting.filterAlpha > 1f) gp.eManager.lighting.filterAlpha = 1f;
        }
        if (counter >= 120) {
            gp.eManager.lighting.filterAlpha -= 0.01f;
            if (gp.eManager.lighting.filterAlpha <= 0f) {
                gp.eManager.lighting.filterAlpha = 0f;
                counter = 0;
                gp.eManager.lighting.dayState = gp.eManager.lighting.day;
                gp.eManager.lighting.dayCounter = 0;
                gp.gameState = gp.playState;
                gp.player.getImage();
            }
        }
    }

    public int getItemIndexOnSlot(int slotCol, int slotRow) {
        return slotCol + (slotRow * 5);
    }

    // CHANGED: Helper methods for text measurement in JavaFX
    public int getXforCenteredText(GraphicsContext gc, String text) {
        Text theText = new Text(text);
        theText.setFont(gc.getFont());
        double width = theText.getLayoutBounds().getWidth();
        return (int) (gp.screenWidth / 2 - width / 2);
    }

    public int getXforAlignToRight(GraphicsContext gc, String text, int tailX) {
        Text theText = new Text(text);
        theText.setFont(gc.getFont());
        double width = theText.getLayoutBounds().getWidth();
        return (int) (tailX - width);
    }
}