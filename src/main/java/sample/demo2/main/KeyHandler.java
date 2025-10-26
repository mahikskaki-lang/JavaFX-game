package sample.demo2.main;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyHandler {
    GamePanel gp;
    public boolean upPressed, downPressed, leftPressed, rightPressed, enterPressed, shotKeyPressed, spacePressed;
    public boolean showDebugText = false;
    public boolean godModeOn = false;

    public KeyHandler(GamePanel gp) { this.gp = gp; }

    public void titleState(KeyCode code) {
        if (gp.ui.titleScreenState == 0) {
            if (code == KeyCode.W) { gp.ui.commandNum--; if (gp.ui.commandNum < 0) gp.ui.commandNum = 3; }
            if (code == KeyCode.S) { gp.ui.commandNum++; if (gp.ui.commandNum > 3) gp.ui.commandNum = 0; }
            if (code == KeyCode.ENTER) {
                if (gp.ui.commandNum == 0) { gp.ui.titleScreenState = 1; }  // New Game
                if (gp.ui.commandNum == 1) { gp.loadUserGame(); }           // Load Game
                if (gp.ui.commandNum == 2) { gp.ui.titleScreenState = 2; gp.ui.commandNum = 0; } // Multiplayer
                if (gp.ui.commandNum == 3) { System.exit(0); }              // Quit
            }
        }
        else if (gp.ui.titleScreenState == 1) {
            if (code == KeyCode.W) { gp.ui.commandNum--; if (gp.ui.commandNum < 0) gp.ui.commandNum = 3; }
            if (code == KeyCode.S) { gp.ui.commandNum++; if (gp.ui.commandNum > 3) gp.ui.commandNum = 0; }
            if (code == KeyCode.ENTER) {
                gp.gameState = gp.playState;
                gp.playMusic(0);
                if (gp.ui.commandNum == 3) { gp.ui.titleScreenState = 0; gp.ui.commandNum = 0; }
            }
        }
        else if (gp.ui.titleScreenState == 2) { // Multiplayer
            if (code == KeyCode.W) { gp.ui.commandNum--; if (gp.ui.commandNum < 0) gp.ui.commandNum = 2; }
            if (code == KeyCode.S) { gp.ui.commandNum++; if (gp.ui.commandNum > 2) gp.ui.commandNum = 0; }
            if (code == KeyCode.ENTER) {
                if (gp.ui.commandNum == 0) {        // Host Game
                    gp.showHostGameDialog();         // Dialog: Start Game / Cancel
                }
                if (gp.ui.commandNum == 1) {        // Join Game
                    gp.showJoinGameDialog();         // Dialog: Join / Cancel
                }
                if (gp.ui.commandNum == 2) {        // Back
                    gp.ui.titleScreenState = 0;
                    gp.ui.commandNum = 0;
                }
            }
        }
    }

    public void keyPressed(KeyEvent event) {
        KeyCode code = event.getCode();

        if (gp.gameState == gp.titleState)            titleState(code);
        else if (gp.gameState == gp.playState)        playState(code);
        else if (gp.gameState == gp.pauseState)       pauseState(code);
        else if (gp.gameState == gp.dialogueState
                || gp.gameState == gp.cutsceneState)    dialogueState(code);
        else if (gp.gameState == gp.characterState)   characterState(code);
        else if (gp.gameState == gp.optionsState)     optionsState(code);
        else if (gp.gameState == gp.gameOverState)    gameOverState(code);
        else if (gp.gameState == gp.tradeState)       tradeState(code);
        else if (gp.gameState == gp.mapState)         mapState(code);
        else if (gp.gameState == gp.chatState)        chatState(event);
    }

    // Chat uses full KeyEvent
    public void chatState(KeyEvent event) {
        KeyCode code = event.getCode();

        if (code == KeyCode.ESCAPE) {
            gp.gameState = gp.playState;
            gp.ui.currentChat = "";
        } else if (code == KeyCode.ENTER) {
            if (!gp.ui.currentChat.isEmpty()) {
                String text = gp.ui.currentChat;
                String full = (gp.player.name != null) ? gp.player.name + ": " + text : "You: " + text;
                gp.ui.chatLog.add(full);
                if (gp.ui.chatLog.size() > 7) gp.ui.chatLog.remove(0);
                if (gp.gameClient != null) gp.gameClient.sendData("MSG:" + text);
                gp.ui.currentChat = "";
            }
        } else if (code == KeyCode.BACK_SPACE) {
            if (!gp.ui.currentChat.isEmpty()) gp.ui.currentChat = gp.ui.currentChat.substring(0, gp.ui.currentChat.length() - 1);
        } else {
            String ch = event.getText();
            if (ch != null && ch.length() == 1 && gp.ui.currentChat.length() < 60) gp.ui.currentChat += ch;
        }
    }

    public void playState(KeyCode code) {
        if (code == KeyCode.W) upPressed = true;
        if (code == KeyCode.S) downPressed = true;
        if (code == KeyCode.A) leftPressed = true;
        if (code == KeyCode.D) rightPressed = true;
        if (code == KeyCode.P) gp.gameState = gp.pauseState;
        if (code == KeyCode.C) gp.gameState = gp.characterState;
        if (code == KeyCode.ENTER) enterPressed = true;
        if (code == KeyCode.F) shotKeyPressed = true;
        if (code == KeyCode.ESCAPE) gp.gameState = gp.optionsState;
        if (code == KeyCode.M) gp.gameState = gp.mapState;
        if (code == KeyCode.X) gp.map.miniMapOn = !gp.map.miniMapOn;
        if (code == KeyCode.SPACE) spacePressed = true;
        if (code == KeyCode.T) { gp.gameState = gp.chatState; gp.ui.unreadMessages = 0; }
        if (code == KeyCode.G) godModeOn = !godModeOn;
    }
    public void pauseState(KeyCode code) { if (code == KeyCode.ESCAPE) gp.gameState = gp.playState; }
    public void dialogueState(KeyCode code) { if (code == KeyCode.ENTER) enterPressed = true; }
    public void characterState(KeyCode code) { if (code == KeyCode.C) gp.gameState = gp.playState; if (code == KeyCode.ENTER) gp.player.selectItem(); playerInventory(code); }

    public void optionsState(KeyCode code) {
        if (code == KeyCode.ESCAPE) { gp.gameState = gp.playState; }
        if (code == KeyCode.ENTER) { enterPressed = true; }
        int maxCommandNum = 0;
        switch (gp.ui.subState) { case 0: maxCommandNum = 5; break; case 3: maxCommandNum = 1; break; }
        if (code == KeyCode.W) { gp.ui.commandNum--; gp.playSE(9); if (gp.ui.commandNum < 0) gp.ui.commandNum = maxCommandNum; }
        if (code == KeyCode.S) { gp.ui.commandNum++; gp.playSE(9); if (gp.ui.commandNum > maxCommandNum) gp.ui.commandNum = 0; }
        if (code == KeyCode.A) {
            if (gp.ui.subState == 0) {
                if (gp.ui.commandNum == 1 && gp.music.volumeScale > 0) { gp.music.volumeScale--; gp.music.checkVolume(); gp.playSE(9); }
                if (gp.ui.commandNum == 2 && gp.se.volumeScale > 0) { gp.se.volumeScale--; gp.playSE(9); }
            }
        }
        if (code == KeyCode.D) {
            if (gp.ui.subState == 0) {
                if (gp.ui.commandNum == 1 && gp.music.volumeScale < 5) { gp.music.volumeScale++; gp.music.checkVolume(); gp.playSE(9); }
                if (gp.ui.commandNum == 2 && gp.se.volumeScale < 5) { gp.se.volumeScale++; gp.playSE(9); }
            }
        }
    }

    public void gameOverState(KeyCode code) {
        if (code == KeyCode.W) { gp.ui.commandNum--; if (gp.ui.commandNum < 0) gp.ui.commandNum = 1; gp.playSE(9); }
        if (code == KeyCode.S) { gp.ui.commandNum++; if (gp.ui.commandNum > 1) gp.ui.commandNum = 0; gp.playSE(9); }
        if (code == KeyCode.ENTER) {
            if (gp.ui.commandNum == 0) { gp.gameState = gp.playState; gp.player.respawn(); gp.playMusic(0); }
            else if (gp.ui.commandNum == 1) { gp.ui.titleScreenState = 0; gp.gameState = gp.titleState; gp.resetGame(true); }
        }
    }

    public void tradeState(KeyCode code) { if (code == KeyCode.ENTER) enterPressed = true; /* rest unchanged */ }
    public void mapState(KeyCode code) { if (code == KeyCode.M) gp.gameState = gp.playState; }

    public void playerInventory(KeyCode code) {
        if (code == KeyCode.W) { if (gp.ui.playerSlotRow != 0) { gp.ui.playerSlotRow--; gp.playSE(1); } }
        if (code == KeyCode.A) { if (gp.ui.playerSlotCol != 0) { gp.ui.playerSlotCol--; gp.playSE(1); } }
        if (code == KeyCode.S) { if (gp.ui.playerSlotRow != 3) { gp.ui.playerSlotRow++; gp.playSE(1); } }
        if (code == KeyCode.D) { if (gp.ui.playerSlotCol != 4) { gp.ui.playerSlotCol++; gp.playSE(1); } }
    }

    public void npcInventory(KeyCode code) {
        if (code == KeyCode.W) { if (gp.ui.npcSlotRow != 0) { gp.ui.npcSlotRow--; gp.playSE(9); } }
        if (code == KeyCode.A) { if (gp.ui.npcSlotCol != 0) { gp.ui.npcSlotCol--; gp.playSE(9); } }
        if (code == KeyCode.S) { if (gp.ui.npcSlotRow != 3) { gp.ui.npcSlotRow++; gp.playSE(9); } }
        if (code == KeyCode.D) { if (gp.ui.npcSlotCol != 4) { gp.ui.npcSlotCol++; gp.playSE(9); } }
    }

    public void keyReleased(KeyCode code) {
        if (code == KeyCode.W) upPressed = false;
        if (code == KeyCode.S) downPressed = false;
        if (code == KeyCode.A) leftPressed = false;
        if (code == KeyCode.D) rightPressed = false;
        if (code == KeyCode.F) shotKeyPressed = false;
        if (code == KeyCode.ENTER) { enterPressed = false; gp.player.attackCanceled = false; }
        if (code == KeyCode.SPACE) spacePressed = false;
    }
}