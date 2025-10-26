package sample.demo2.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private GameServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String clientId;

    public ClientHandler(Socket socket, GameServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.clientId = "Player" + socket.hashCode();
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            sendMessage("ID:" + this.clientId);
            server.syncPlayersToNewClient(this);
            server.syncMonstersToNewClient(this);
            server.syncObjectsToNewClient(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                String[] parts = clientMessage.split(":", 2);
                String command = parts[0];
                String data = parts.length > 1 ? parts[1] : "";

                switch(command) {
                    case "POS":
                        server.broadcast("PLAYER_POS:" + this.clientId + ":" + data, this);
                        String[] posParts = data.split(":");
                        if (posParts.length == 3) {
                            int x = Integer.parseInt(posParts[0]);
                            int y = Integer.parseInt(posParts[1]);
                            String dir = posParts[2];
                            server.getGamePanel().addOrUpdatePlayer(this.clientId, x, y, dir);
                        }
                        break;
                    case "MSG":
                        server.broadcast("PLAYER_MSG:" + this.clientId + ": " + data, this);
                        break;
                    case "ATK":
                        server.handleAttack(this, data);
                        break;
                    case "OBJ_PICKUP":
                        server.handleObjectPickup(data, this);
                        break;
                    case "EQUIP":
                        server.updatePlayerEquipment(this, data);
                        break;
                    case "PROJ":
                        server.handleProjectileSpawn(this, data);
                        break;
                    case "PLAYER_DIED":
                        System.out.println("Server received death notification from: " + this.clientId);
                        server.broadcast("PLAYER_REMOVE:" + this.clientId, this);
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println(clientId + " disconnected.");
        } finally {
            server.removeClient(this);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) { out.println(message); }
    public String getClientId() { return clientId; }
}