package sample.demo2.main;

import sample.demo2.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClient implements Runnable {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GamePanel gp;

    public GameClient(String ipAddress, int port, GamePanel gp) {
        this.gp = gp;
        try {
            this.socket = new Socket(ipAddress, port);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to the server.");
        } catch (IOException e) {
            System.err.println("Could not connect to server: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                parseServerMessage(serverMessage);
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server.");
        }
    }

    private void parseServerMessage(String message) {
        String[] parts = message.split(":", 2);
        String command = parts[0];
        String payload = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "ID": {
                gp.player.name = payload; // adopt server ID always
                System.out.println("Assigned Player ID: " + gp.player.name);
                String w = (gp.player.currentWeapon != null) ? gp.player.currentWeapon.name : "NA";
                String s = (gp.player.currentShield != null) ? gp.player.currentShield.name : "NA";
                String l = (gp.player.currentLight  != null) ? gp.player.currentLight.name  : "NA";
                sendData("EQUIP:" + w + ":" + s + ":" + l);
                break;
            }
            case "PLAYER_POS": {
                String[] posParts = payload.split(":");
                if (posParts.length == 4) {
                    String playerId = posParts[0];
                    int x = Integer.parseInt(posParts[1]);
                    int y = Integer.parseInt(posParts[2]);
                    String direction = posParts[3];
                    gp.addOrUpdatePlayer(playerId, x, y, direction);
                }
                break;
            }
            case "PLAYER_EQUIP": {
                String[] eq = payload.split(":");
                if (eq.length == 4) {
                    String playerId = eq[0];
                    String w = eq[1], s = eq[2], l = eq[3];
                    if (gp.player.name != null && gp.player.name.equals(playerId)) break;
                    if (gp.otherPlayers.containsKey(playerId)) {
                        Player p = gp.otherPlayers.get(playerId);
                        javafx.application.Platform.runLater(() -> gp.applyEquipment(p, w, s, l));
                    }
                }
                break;
            }
            case "PLAYER_MSG":
                gp.ui.receiveChatMessage(payload);
                break;
            case "PLAYER_REMOVE":
                gp.removePlayer(payload);
                break;
            case "MONSTER_UPDATE": {
                String[] monsterParts = payload.split(":");
                if (monsterParts.length == 4) {
                    int index = Integer.parseInt(monsterParts[0]);
                    int x = Integer.parseInt(monsterParts[1]);
                    int y = Integer.parseInt(monsterParts[2]);
                    int life = Integer.parseInt(monsterParts[3]);
                    gp.updateMonsterState(index, x, y, life);
                }
                break;
            }
            case "MONSTER_DEATH":
                gp.killMonster(Integer.parseInt(payload));
                break;

            case "OBJ_CLEAR":
                javafx.application.Platform.runLater(() -> {
                    for (int i = 0; i < gp.obj[gp.currentMap].length; i++) {
                        gp.obj[gp.currentMap][i] = null;
                    }
                });
                break;

            case "OBJ_SPAWN": {
                String[] objParts = payload.split(":");
                if (objParts.length == 4) {
                    int index2 = Integer.parseInt(objParts[0]);
                    String name = objParts[1];
                    int worldX = Integer.parseInt(objParts[2]);
                    int worldY = Integer.parseInt(objParts[3]);
                    gp.spawnObject(index2, name, worldX, worldY);
                }
                break;
            }

            case "OBJ_REMOVE":
                gp.removeObject(Integer.parseInt(payload));
                break;

            // attackerId:name:x:y:dir
            case "PROJ_SPAWN": {
                String[] pp = payload.split(":");
                if (pp.length == 5) {
                    String attackerId = pp[0];
                    String projName = pp[1];
                    int x = Integer.parseInt(pp[2]);
                    int y = Integer.parseInt(pp[3]);
                    String dir = pp[4];

                    javafx.application.Platform.runLater(() -> {
                        Player owner = (gp.player.name != null && gp.player.name.equals(attackerId))
                                ? gp.player
                                : gp.otherPlayers.get(attackerId);
                        gp.spawnProjectile(projName, x, y, dir, owner);
                    });
                }
                break;
            }

            case "PLAYER_ATTACK_ANIM":
                gp.triggerAttackAnimation(payload);
                break;

            case "NPC_UPDATE": {
                String[] npcParts = payload.split(":");
                if (npcParts.length == 4) {
                    int idx = Integer.parseInt(npcParts[0]);
                    int x = Integer.parseInt(npcParts[1]);
                    int y = Integer.parseInt(npcParts[2]);
                    String direction = npcParts[3];
                    gp.updateNpcState(idx, x, y, direction);
                }
                break;
            }

            case "ITILE_UPDATE": {
                String[] tileParts = payload.split(":");
                if (tileParts.length == 2) {
                    int idx = Integer.parseInt(tileParts[0]);
                    int life = Integer.parseInt(tileParts[1]);
                    gp.updateInteractiveTile(idx, life);
                }
                break;
            }
        }
    }

    public void sendData(String data) {
        if(out != null) out.println(data);
    }
}