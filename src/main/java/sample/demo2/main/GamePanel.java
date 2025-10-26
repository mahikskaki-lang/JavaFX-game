package sample.demo2.main;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import sample.demo2.ai.PathFinder;
import sample.demo2.data.SaveLoad;
import sample.demo2.entity.Entity;
import sample.demo2.entity.Player;
import sample.demo2.environment.EnvironmentManager;
import sample.demo2.tile.Map;
import sample.demo2.tile.TileManager;
import sample.demo2.tile_interactive.InteractiveTile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GamePanel extends Pane {
    // SCREEN SETTINGS
    final int originalTileSize = 16;
    final int scale = 3;

    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 20;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    // WORLD SETTINGS
    public int maxWorldCol;
    public int maxWorldRow;
    public final int maxMap = 10;
    public int currentMap = 0;

    // FPS
    int FPS = 60;
    public boolean fullScreenOn = false;

    Canvas canvas;
    GraphicsContext gc;
    AnimationTimer gameLoop;

    // SYSTEM
    public TileManager tileM = new TileManager(this);
    public KeyHandler keyH = new KeyHandler(this);
    public EventHandler eHandler = new EventHandler(this);
    Sound music = new Sound();
    Sound se = new Sound();
    public CollisionChecker cChecker = new CollisionChecker(this);
    public AssetSetter aSetter = new AssetSetter(this);
    public UI ui = new UI(this);
    Config config = new Config(this);
    public PathFinder pFinder = new PathFinder(this);
    EnvironmentManager eManager = new EnvironmentManager(this);
    Map map = new Map(this);
    SaveLoad saveLoad = new SaveLoad(this);
    public EntityGenerator eGenerator = new EntityGenerator(this);
    public CutsceneManager csManager = new CutsceneManager(this);

    // ENTITY AND OBJECT
    public Player player = new Player(this, keyH);
    public Entity obj[][] = new Entity[maxMap][20];
    public Entity npc[][] = new Entity[maxMap][10];
    public Entity monster[][] = new Entity[maxMap][20];
    public InteractiveTile iTile[][] = new InteractiveTile[maxMap][50];
    public Entity projectile[][] = new Entity[maxMap][20];
    public ArrayList<Entity> particleList = new ArrayList<>();
    ArrayList<Entity> entityList = new ArrayList<>();

    // GAME STATE
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int dialogueState = 3;
    public final int characterState = 4;
    public final int optionsState = 5;
    public final int gameOverState = 6;
    public final int transitionState = 7;
    public final int tradeState = 8;
    public final int sleepState = 9;
    public final int mapState = 10;
    public final int cutsceneState = 11;
    public final int chatState = 12;

    // MULTIPLAYER
    public GameServer gameServer;
    public GameClient gameClient;
    public String loggedInUsername;
    public ConcurrentHashMap<String, Player> otherPlayers = new ConcurrentHashMap<>();

    // OTHERS
    public boolean bossBattleOn = false;

    // AREA
    public int currentArea;
    public int nextArea;
    public final int outside = 50;
    public final int indoor = 51;
    public final int dungeon = 52;

    // LOADING OVERLAY
    private StackPane loadingOverlay;
    private Label loadingLabel;
    private ProgressIndicator loadingSpinner;

    public GamePanel() {
        this.setPrefSize(screenWidth, screenHeight);
        this.canvas = new Canvas(screenWidth, screenHeight);
        this.gc = canvas.getGraphicsContext2D();
        this.getChildren().add(canvas);

        initLoadingOverlay(); // overlay above the canvas
    }

    // ---------- Loading Overlay ----------
    private void initLoadingOverlay() {
        loadingOverlay = new StackPane();
        loadingOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        loadingOverlay.setVisible(false);
        loadingOverlay.setPickOnBounds(true);
        loadingOverlay.setManaged(false);

        loadingOverlay.prefWidthProperty().bind(widthProperty());
        loadingOverlay.prefHeightProperty().bind(heightProperty());

        loadingSpinner = new ProgressIndicator();
        loadingSpinner.setPrefSize(64, 64);

        loadingLabel = new Label("Loading game...");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        VBox box = new VBox(12, loadingSpinner, loadingLabel);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));

        loadingOverlay.getChildren().add(box);
        getChildren().add(loadingOverlay);
    }

    public void showLoadingOverlay(String message) {
        Platform.runLater(() -> {
            loadingLabel.setText(message != null ? message : "Loading...");
            loadingOverlay.setVisible(true);
            loadingOverlay.toFront();
        });
    }

    public void hideLoadingOverlay() {
        Platform.runLater(() -> loadingOverlay.setVisible(false));
    }

    // Call this instead of setupGame()+startGameLoop() to get a smooth scene switch
    public void startGameWithOverlay() {
        showLoadingOverlay("Loading game...");
        PauseTransition pt = new PauseTransition(Duration.millis(80));
        pt.setOnFinished(ev -> {
            setupGame();
            startGameLoop();
            hideLoadingOverlay();
        });
        pt.play();
    }
    // ------------------------------------

    public void setupGame() {
        aSetter.setObject();
        aSetter.setNPC();
        aSetter.setMonster();
        aSetter.setInteractiveTile();
        eManager.setup();
        gameState = titleState;
    }

    public void resetGame(boolean restart) {
        stopMusic();
        currentArea = outside;
        removeTempEntity();
        bossBattleOn = false;
        player.setDefaultPositions();
        player.restoreStatus();
        aSetter.setMonster();
        aSetter.setNPC();
        player.resetCounter();

        if (restart) {
            player.setDefaultValues();
            aSetter.setObject();
            aSetter.setInteractiveTile();
            eManager.lighting.resetDay();
        }
    }

    public void handleMouseClick(double x, double y) {
        if (gameState == playState) {
            if (ui.chatNotificationArea.contains(x, y)) {
                gameState = chatState;
                ui.unreadMessages = 0;
            }
        }
    }

    public void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render(gc);
            }
        };
        gameLoop.start();
    }

    // Spawn a projectile by name, used by server and clients
    public void spawnProjectile(String name, int worldX, int worldY, String direction, Entity owner) {
        Entity e = eGenerator.getObject(name);
        if (e instanceof sample.demo2.entity.Projectile) {
            sample.demo2.entity.Projectile proj = (sample.demo2.entity.Projectile) e;
            proj.set(worldX, worldY, direction, true, (owner != null ? owner : player));
            for (int i = 0; i < projectile[1].length; i++) {
                if (projectile[currentMap][i] == null) {
                    projectile[currentMap][i] = proj;
                    break;
                }
            }
        }
    }

    // Apply equipment to any Player (local or remote)
    public void applyEquipment(Player p, String weaponName, String shieldName, String lightName) {
        if (weaponName != null && !"NA".equals(weaponName)) p.currentWeapon = eGenerator.getObject(weaponName);
        if (shieldName != null && !"NA".equals(shieldName)) p.currentShield = eGenerator.getObject(shieldName);
        if (lightName != null) {
            p.currentLight = !"NA".equals(lightName) ? eGenerator.getObject(lightName) : null;
            if (p == player) player.lightUpdated = true;
        }
        p.attack = p.getAttack();
        p.defense = p.getDefense();
        p.getAttackImage();
        p.getGuardImage();
    }

    public void update() {
        if (gameState == playState) {
            // Local player
            player.update();

            // Remote players
            for (Entity other : otherPlayers.values()) {
                other.update();
            }

            // Client sends own POS
            if (gameClient != null) {
                String playerData = "POS:" + player.worldX + ":" + player.worldY + ":" + player.direction;
                gameClient.sendData(playerData);
            }

            // NPCs
            for (int i = 0; i < npc[1].length; i++) {
                if (npc[currentMap][i] != null) {
                    npc[currentMap][i].update();
                    if (gameServer != null) {
                        gameServer.broadcastNpcUpdate(i, npc[currentMap][i]);
                    }
                }
            }

            // Monsters
            for (int i = 0; i < monster[1].length; i++) {
                if (monster[currentMap][i] != null) {
                    if (monster[currentMap][i].alive && !monster[currentMap][i].dying) {
                        monster[currentMap][i].update();
                        if (gameServer != null) {
                            gameServer.broadcastMonsterUpdate(i, monster[currentMap][i]);
                        }
                    }
                    if (!monster[currentMap][i].alive) {
                        // Always call checkDrop(): SP drops locally; host spawns; guest no-op
                        monster[currentMap][i].checkDrop();

                        // Multiplayer: broadcast death + snapshot so guests always see drops
                        if (gameServer != null) {
                            gameServer.broadcastMonsterDeath(i);
                            gameServer.broadcastObjectsSnapshot();
                        }

                        // Remove monster locally
                        monster[currentMap][i] = null;
                    }
                }
            }

            // Projectiles
            for (int i = 0; i < projectile[1].length; i++) {
                if (projectile[currentMap][i] != null) {
                    if (projectile[currentMap][i].alive) {
                        projectile[currentMap][i].update();
                    }
                    if (!projectile[currentMap][i].alive) {
                        projectile[currentMap][i] = null;
                    }
                }
            }

            // Particles
            for (int i = 0; i < particleList.size(); i++) {
                if (particleList.get(i) != null) {
                    if (particleList.get(i).alive) {
                        particleList.get(i).update();
                    }
                    if (!particleList.get(i).alive) {
                        particleList.remove(i);
                    }
                }
            }

            // Interactive tiles
            for (int i = 0; i < iTile[1].length; i++) {
                if (iTile[currentMap][i] != null) iTile[currentMap][i].update();
            }

            eManager.update();
        }
    }

    public void removeObject(int index) {
        Platform.runLater(() -> obj[currentMap][index] = null);
    }

    public void updateNpcState(int index, int x, int y, String direction) {
        Platform.runLater(() -> {
            if (npc[currentMap][index] != null) {
                if (gameServer == null) {
                    npc[currentMap][index].worldX = x;
                    npc[currentMap][index].worldY = y;
                    npc[currentMap][index].direction = direction;
                }
            }
        });
    }

    public void render(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, screenWidth, screenHeight);

        if (gameState == titleState) {
            ui.draw(gc);
        } else if (gameState == mapState) {
            map.drawFullMapScreen(gc);
        } else {
            tileM.draw(gc);

            for (InteractiveTile it : iTile[currentMap]) {
                if (it != null) it.draw(gc);
            }

            entityList.add(player);
            entityList.addAll(otherPlayers.values());
            for (Entity e : npc[currentMap]) { if (e != null) entityList.add(e); }
            for (Entity e : obj[currentMap]) { if (e != null) entityList.add(e); }
            for (Entity e : monster[currentMap]) { if (e != null) entityList.add(e); }
            for (Entity e : projectile[currentMap]) { if (e != null) entityList.add(e); }
            for (Entity e : particleList) { if (e != null) entityList.add(e); }

            Collections.sort(entityList, Comparator.comparingInt(e -> e.worldY));
            for (Entity e : entityList) e.draw(gc);
            entityList.clear();

            eManager.draw(gc);
            map.drawMiniMap(gc);
            csManager.draw(gc);
            ui.draw(gc);
        }
    }

    // Server start/join
    public void startGameServer() {
        gameServer = new GameServer(this);
        new Thread(gameServer).start();
        joinGameServer("localhost"); // host also connects as a client
    }
    public void joinGameServer(String ipAddress) {
        gameClient = new GameClient(ipAddress, 7777, this);
        new Thread(gameClient).start();
    }
    // Overload to support custom port
    public void joinGameServer(String ipAddress, int port) {
        gameClient = new GameClient(ipAddress, port, this);
        new Thread(gameClient).start();
    }
    // Supports "IP" or "IP:port"
    public void joinGameServerFromCode(String joinCode) {
        String code = (joinCode == null) ? "" : joinCode.trim();
        if (code.isEmpty()) return;
        String host = code;
        int port = 7777;
        int idx = code.indexOf(':');
        if (idx > 0) {
            host = code.substring(0, idx);
            try { port = Integer.parseInt(code.substring(idx + 1)); }
            catch (NumberFormatException ignored) {}
        }
        joinGameServer(host, port);
    }

    public void addOrUpdatePlayer(String playerId, int x, int y, String direction) {
        Platform.runLater(() -> {
            if (player.name != null && playerId.equals(player.name)) return;

            if (!otherPlayers.containsKey(playerId)) {
                Player newPlayer = new Player(this, null);
                newPlayer.name = playerId;
                otherPlayers.put(playerId, newPlayer);
                System.out.println("Added new remote player: " + playerId);
            }

            Entity p = otherPlayers.get(playerId);
            if (p != null) {
                p.worldX = x;
                p.worldY = y;
                p.direction = direction;
            }
        });
    }

    public void removePlayer(String playerId) {
        Platform.runLater(() -> {
            if (otherPlayers.containsKey(playerId)) {
                otherPlayers.remove(playerId);
                System.out.println("Removed player: " + playerId);
            }
        });
    }

    public void triggerAttackAnimation(String playerId) {
        Platform.runLater(() -> {
            Player target = null;
            if (player.name != null && player.name.equals(playerId)) target = player;
            else if (otherPlayers.containsKey(playerId)) target = otherPlayers.get(playerId);
            if (target != null) {
                target.attacking = true;
                target.spriteCounter = 0;
            }
        });
    }

    public void updateMonsterState(int index, int x, int y, int life) {
        Platform.runLater(() -> {
            if (monster[currentMap][index] != null) {
                if (gameServer == null) {
                    monster[currentMap][index].worldX = x;
                    monster[currentMap][index].worldY = y;
                }
                monster[currentMap][index].life = life;
            }
        });
    }

    public void killMonster(int index) {
        Platform.runLater(() -> {
            if (monster[currentMap][index] != null) {
                monster[currentMap][index] = null;
            }
        });
    }

    public void playMusic(int i) { music.setFile(i); music.play(); music.loop(); }
    public void stopMusic() { music.stop(); }
    public void playSE(int i) { se.setFile(i); se.play(); }

    public void changeArea() {
        if (nextArea != currentArea) {
            stopMusic();
            if (nextArea == outside) playMusic(0);
            if (nextArea == indoor) playMusic(18);
            if (nextArea == dungeon) playMusic(19);
            aSetter.setNPC();
        }
        currentArea = nextArea;
        aSetter.setMonster();
    }

    public void removeTempEntity() {
        for (int mapNum = 0; mapNum < maxMap; mapNum++) {
            for (int i = 0; i < obj[1].length; i++) {
                if (obj[mapNum][i] != null && obj[mapNum][i].temp) {
                    obj[mapNum][i] = null;
                }
            }
        }
    }

    public void updateInteractiveTile(int index, int life) {
        Platform.runLater(() -> {
            InteractiveTile tile = iTile[currentMap][index];
            if (tile != null) {
                tile.life = life;
                if (tile.life <= 0) {
                    iTile[currentMap][index] = tile.getDestroyedForm();
                }
            }
        });
    }

    public void spawnObject(int index, String name, int worldX, int worldY) {
        Platform.runLater(() -> {
            Entity newItem = eGenerator.getObject(name);
            if (newItem != null) {
                newItem.worldX = worldX;
                newItem.worldY = worldY;
                obj[currentMap][index] = newItem;
            }
        });
    }

    public void loadUserGame() {
        try {
            File file = new File("last_login.txt");
            if (!file.exists()) {
                System.out.println("No last user found. Cannot load game.");
                return;
            }

            BufferedReader br = new BufferedReader(new java.io.FileReader(file));
            String username = br.readLine();
            br.close();

            if (username != null && !username.trim().isEmpty()) {
                this.loggedInUsername = username;
                System.out.println("Attempting to load game for user: " + username);
                saveLoad.load();
                gameState = playState;

                stopMusic();
                if (currentArea == outside)      playMusic(0);
                else if (currentArea == indoor)  playMusic(18);
                else if (currentArea == dungeon) playMusic(19);
                else                              playMusic(0);
            } else {
                System.out.println("last_login.txt is empty. Cannot load game.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load user game.");
        }
    }

    // ---------- Host/Join dialogs ----------
    public List<String> getLocalIPv4Addresses() {
        List<String> ips = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface ni = nets.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    String host = addr.getHostAddress();
                    if (!addr.isLoopbackAddress() && host.indexOf(':') < 0) {
                        ips.add(host);
                    }
                }
            }
        } catch (Exception ignored) { }
        if (ips.isEmpty()) ips.add("127.0.0.1");
        return ips;
    }

    private void copyToClipboard(String text) {
        Clipboard cb = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        cb.setContent(content);
    }

    // Host Game dialog (before starting server)
    public void showHostGameDialog() {
        List<String> ips = getLocalIPv4Addresses();
        String defaultIp = ips.get(0);

        Label header = new Label("Share this Join Code with your friend!");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label joinLabel = new Label("Join Code:");
        joinLabel.setStyle("-fx-font-size: 13px;");

        TextField joinCodeField = new TextField(defaultIp);
        joinCodeField.setEditable(false);
        joinCodeField.setPrefColumnCount(16);
        joinCodeField.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button copyBtn = new Button("Copy");
        copyBtn.setOnAction(e -> copyToClipboard(joinCodeField.getText()));

        HBox codeRow = new HBox(10, joinLabel, joinCodeField, copyBtn);
        codeRow.setAlignment(Pos.CENTER_LEFT);

        Button startBtn = new Button("Start Game");
        Button cancelBtn = new Button("Cancel");

        HBox actions = new HBox(10, startBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(12, header, codeRow, actions);
        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color: white;");

        Stage dialog = new Stage();
        dialog.initOwner(this.getScene() != null ? this.getScene().getWindow() : null);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Host Game");
        dialog.setScene(new Scene(content, 520, 160));
        dialog.setResizable(false);

        startBtn.setDefaultButton(true);
        startBtn.setOnAction(e -> {
            copyToClipboard(joinCodeField.getText());
            dialog.close();
            startGameServer();
            gameState = playState;
            playMusic(0);
        });
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> dialog.close());

        dialog.show();
    }

    // Join Game dialog (enter IP or IP:port)
    public void showJoinGameDialog() {
        Label header = new Label("Enter the host's Join Code:");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField input = new TextField();
        input.setPromptText("e.g., 192.168.1.103 or 192.168.1.103:7777");
        input.setPrefColumnCount(20);

        Button joinBtn = new Button("Join");
        Button cancelBtn = new Button("Cancel");

        HBox actions = new HBox(10, joinBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(12, header, input, actions);
        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color: white;");

        Stage dialog = new Stage();
        dialog.initOwner(this.getScene() != null ? this.getScene().getWindow() : null);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Join Game");
        dialog.setScene(new Scene(content, 520, 160));
        dialog.setResizable(false);

        joinBtn.setDefaultButton(true);
        joinBtn.setOnAction(e -> {
            String code = input.getText() != null ? input.getText().trim() : "";
            if (!code.isEmpty()) {
                joinGameServerFromCode(code);
                gameState = playState;
                playMusic(0);
                dialog.close();
            }
        });
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> dialog.close());

        dialog.show();
    }
}