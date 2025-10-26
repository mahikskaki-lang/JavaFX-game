package sample.demo2.main;

import javafx.application.Platform;
import sample.demo2.entity.Entity;
import sample.demo2.entity.Player;

import java.awt.Rectangle;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer implements Runnable {

    private ServerSocket serverSocket;
    private GamePanel gp;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public GameServer(GamePanel gp) {
        this.gp = gp;
        try {
            this.serverSocket = new ServerSocket(7777);
            System.out.println("Server started. Waiting for clients...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Server socket closed.");
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("Client disconnected: " + clientHandler.getClientId());
        broadcast("PLAYER_REMOVE:" + clientHandler.getClientId(), null);
        gp.removePlayer(clientHandler.getClientId());
    }

    public GamePanel getGamePanel() { return gp; }

    // PvM attack handling (no PvP)
    public synchronized void handleAttack(ClientHandler attackerHandler, String direction) {
        Player attackerPlayer = getPlayerById(attackerHandler.getClientId());
        if (attackerPlayer == null) return;

        broadcast("PLAYER_ATTACK_ANIM:" + attackerPlayer.name, null);

        Rectangle attackArea = new Rectangle(0, 0, 0, 0);
        int attackAreaDefaultX = attackerPlayer.attackArea.x;
        int attackAreaDefaultY = attackerPlayer.attackArea.y;

        switch (direction) {
            case "up":
                attackArea.x = attackerPlayer.worldX + attackAreaDefaultX;
                attackArea.y = attackerPlayer.worldY + attackAreaDefaultY - attackerPlayer.attackArea.height;
                break;
            case "down":
                attackArea.x = attackerPlayer.worldX + attackAreaDefaultX;
                attackArea.y = attackerPlayer.worldY + attackAreaDefaultY + attackerPlayer.attackArea.height;
                break;
            case "left":
                attackArea.x = attackerPlayer.worldX + attackAreaDefaultX - attackerPlayer.attackArea.width;
                attackArea.y = attackerPlayer.worldY + attackAreaDefaultY;
                break;
            case "right":
                attackArea.x = attackerPlayer.worldX + attackAreaDefaultX + attackerPlayer.attackArea.width;
                attackArea.y = attackerPlayer.worldY + attackAreaDefaultY;
                break;
        }
        attackArea.width = attackerPlayer.attackArea.width;
        attackArea.height = attackerPlayer.attackArea.height;

        // Only monsters take damage
        for (int i = 0; i < gp.monster[gp.currentMap].length; i++) {
            Entity monster = gp.monster[gp.currentMap][i];
            if (monster != null && !monster.invincible) {
                Rectangle monsterArea = new Rectangle(
                        monster.worldX + monster.solidArea.x,
                        monster.worldY + monster.solidArea.y,
                        monster.solidArea.width,
                        monster.solidArea.height
                );
                if (attackArea.intersects(monsterArea)) {
                    handleMonsterDamage(String.valueOf(i), attackerHandler);
                    break;
                }
            }
        }
    }

    public synchronized void handleMonsterDamage(String data, ClientHandler attackerHandler) {
        try {
            int monsterIndex = Integer.parseInt(data);
            Player attackerPlayer = getPlayerById(attackerHandler.getClientId());
            Entity monster = gp.monster[gp.currentMap][monsterIndex];

            if (monster != null && !monster.invincible && attackerPlayer != null) {
                int dmg = attackerPlayer.attack - monster.defense;
                if (dmg < 1) dmg = 1;

                final int damageToApply = dmg;
                final int idx = monsterIndex;
                final Entity targetMonster = monster;

                Platform.runLater(() -> {
                    targetMonster.life -= damageToApply;
                    targetMonster.invincible = true;
                    targetMonster.damageReaction();
                    if (targetMonster.life <= 0 && !targetMonster.dying) {
                        targetMonster.dying = true;
                        targetMonster.spriteCounter = 0;
                    }
                    broadcastMonsterUpdate(idx, targetMonster);
                });
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public synchronized void handleObjectPickup(String data, ClientHandler clientHandler) {
        try {
            int objIndex = Integer.parseInt(data);
            Platform.runLater(() -> {
                if (gp.obj[gp.currentMap][objIndex] != null) {
                    gp.obj[gp.currentMap][objIndex] = null;
                    broadcast("OBJ_REMOVE:" + objIndex, null);
                }
            });
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // EQUIP sync
    public void updatePlayerEquipment(ClientHandler sender, String data) {
        String[] p = data.split(":");
        String w = p.length > 0 ? p[0] : "NA";
        String s = p.length > 1 ? p[1] : "NA";
        String l = p.length > 2 ? p[2] : "NA";

        Platform.runLater(() -> {
            Player player = getPlayerById(sender.getClientId());
            if (player != null) {
                gp.applyEquipment(player, w, s, l);
                broadcast("PLAYER_EQUIP:" + sender.getClientId() + ":" + w + ":" + s + ":" + l, null);
            }
        });
    }

    // Projectile from a client: spawn on server GP and broadcast to others
    public void handleProjectileSpawn(ClientHandler sender, String data) {
        String[] p = data.split(":");
        if (p.length != 4) return;
        String projName = p[0];
        int x = Integer.parseInt(p[1]);
        int y = Integer.parseInt(p[2]);
        String dir = p[3];

        Platform.runLater(() -> {
            Player owner = getPlayerById(sender.getClientId());
            gp.spawnProjectile(projName, x, y, dir, owner);
            // Broadcast to everyone EXCEPT the sender client
            broadcast("PROJ_SPAWN:" + sender.getClientId() + ":" + projName + ":" + x + ":" + y + ":" + dir, sender);
        });
    }

    public void broadcastNpcUpdate(int npcIndex, Entity npc) {
        if (npc != null) {
            String message = "NPC_UPDATE:" + npcIndex + ":" + npc.worldX + ":" + npc.worldY + ":" + npc.direction;
            broadcast(message, null);
        }
    }

    public void broadcastMonsterUpdate(int monsterIndex, Entity monster) {
        if (monster != null) {
            String message = "MONSTER_UPDATE:" + monsterIndex + ":" + monster.worldX + ":" + monster.worldY + ":" + monster.life;
            broadcast(message, null);
        }
    }

    public void broadcastMonsterDeath(int monsterIndex) {
        String message = "MONSTER_DEATH:" + monsterIndex;
        broadcast(message, null);
        System.out.println("Server broadcasting death for monster index: " + monsterIndex);
    }

    private Player getPlayerById(String playerId) {
        if (gp.player.name != null && gp.player.name.equals(playerId)) return gp.player;
        return gp.otherPlayers.get(playerId);
    }

    public void syncPlayersToNewClient(ClientHandler newClient) {
        Player hostPlayer = gp.player;
        if (hostPlayer.name != null) {
            newClient.sendMessage("PLAYER_POS:" + hostPlayer.name + ":" + hostPlayer.worldX + ":" + hostPlayer.worldY + ":" + hostPlayer.direction);
            String w = (hostPlayer.currentWeapon != null) ? hostPlayer.currentWeapon.name : "NA";
            String s = (hostPlayer.currentShield != null) ? hostPlayer.currentShield.name : "NA";
            String l = (hostPlayer.currentLight  != null) ? hostPlayer.currentLight.name  : "NA";
            newClient.sendMessage("PLAYER_EQUIP:" + hostPlayer.name + ":" + w + ":" + s + ":" + l);
        }

        for (ClientHandler otherClient : clients) {
            if (otherClient != newClient) {
                Player otherPlayer = getPlayerById(otherClient.getClientId());
                if (otherPlayer != null) {
                    newClient.sendMessage("PLAYER_POS:" + otherPlayer.name + ":" + otherPlayer.worldX + ":" + otherPlayer.worldY + ":" + otherPlayer.direction);
                    String w = (otherPlayer.currentWeapon != null) ? otherPlayer.currentWeapon.name : "NA";
                    String s = (otherPlayer.currentShield != null) ? otherPlayer.currentShield.name : "NA";
                    String l = (otherPlayer.currentLight  != null) ? otherPlayer.currentLight.name  : "NA";
                    newClient.sendMessage("PLAYER_EQUIP:" + otherPlayer.name + ":" + w + ":" + s + ":" + l);
                }
            }
        }
    }

    public void syncMonstersToNewClient(ClientHandler newClient) {
        for (int i = 0; i < gp.monster[gp.currentMap].length; i++) {
            Entity monster = gp.monster[gp.currentMap][i];
            if (monster != null) {
                newClient.sendMessage("MONSTER_UPDATE:" + i + ":" + monster.worldX + ":" + monster.worldY + ":" + monster.life);
            }
        }
    }

    public void syncObjectsToNewClient(ClientHandler newClient) {
        newClient.sendMessage("OBJ_CLEAR");
        for (int i = 0; i < gp.obj[gp.currentMap].length; i++) {
            Entity o = gp.obj[gp.currentMap][i];
            if (o != null) {
                newClient.sendMessage("OBJ_SPAWN:" + i + ":" + o.name + ":" + o.worldX + ":" + o.worldY);
            }
        }
    }

    // NEW: broadcast a full object snapshot to all clients
    public void broadcastObjectsSnapshot() {
        for (ClientHandler c : clients) {
            syncObjectsToNewClient(c);
        }
    }

    // Authoritative spawn at coords (used by Entity.checkDrop on server)
    public void spawnObjectAt(String name, int worldX, int worldY) {
        Platform.runLater(() -> {
            int idx = -1;
            for (int i = 0; i < gp.obj[gp.currentMap].length; i++) {
                if (gp.obj[gp.currentMap][i] == null) { idx = i; break; }
            }
            if (idx == -1) return;

            Entity newItem = gp.eGenerator.getObject(name);
            if (newItem == null) return;

            newItem.worldX = worldX;
            newItem.worldY = worldY;
            gp.obj[gp.currentMap][idx] = newItem;

            // still broadcast the single spawn (fast path)
            broadcast("OBJ_SPAWN:" + idx + ":" + name + ":" + worldX + ":" + worldY, null);
        });
    }

    public void spawnObject(Entity obj) {
        if (obj == null) return;
        spawnObjectAt(obj.name, obj.worldX, obj.worldY);
    }
}