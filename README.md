# **All Of Us Are Dead: A JavaFX Action RPG**

This report provides a technical and feature overview of **“All Of Us Are Dead,”** a 2D top-down action RPG designed for LAN multiplayer using the JavaFX framework.

## **1\. Project Introduction and Purpose**

The primary goal of this project was to demonstrate smooth, feature-rich real-time game development within the JavaFX environment.

### **Core Objectives:**

* Deliver **smooth real-time gameplay** (movement, combat, AI, projectiles, particles) on a JavaFX **Canvas**.  
* Implement a **complete RPG loop**: inventory/equipment, trading, dynamic lighting (day/night), puzzles, cutscenes, and a climactic boss fight.  
* Enable **multiplayer over LAN (Wi-Fi)** with Host/Join functionality and in-game chat.  
* Support **user accounts** (login/signup with OTP email) and **per-user save/load persistence**.

## **2\. Framework and Technology Stack**

The project relies on established Java technologies for its core functionality, networking, and persistence.

| Component | Technology | Description |
| :---- | :---- | :---- |
| **Rendering & UI** | **JavaFX** (Canvas/GraphicsContext) | Core rendering engine for the game world, plus all UI elements (Title, Inventory, Dialogs). |
| **Build & Dependencies** | **Java 17 \+ Maven** | Modern Java build environment. |
| **Networking** | **TCP Sockets** (Custom Protocol) | Lightweight, text-based protocol for LAN multiplayer sync. |
| **Persistence** | **File-Based** (.dat, .txt) | File I/O for per-user saves, user accounts (accounts.txt), and configuration (config.txt). |
| **User Authentication** | **Jakarta Mail** (SMTP) | Used to send One-Time Passwords (OTP) during the user signup process. |
| **Audio** | **javax.sound.sampled** | Handles background music and in-game sound effects. |

## **3\. Key Project Features**

### **3.1. Title, Menus, and Flow**

The application supports a full set of game flows, including **New Game**, **Load Game**, **Multiplayer** (Host/Join), **Options**, **Map** view, and the **Game Over** state.

### **3.2. Multiplayer (Same Wi‑Fi / LAN)**

Multiplayer is achieved using a simple client-server model over the local network.

* **Host Dialogue:** Displays the local IP address and port (e.g., 192.168.x.x:7777) for guests to connect.  
* **Join Dialogue:** Accepts the full host:port string to establish a connection.  
* **Live Sync:** The server handles authoritative synchronization of positions, equipment, attacks, projectiles, and monster/object states.  
* **In-Game Chat:** Includes an unread notification badge.

### **3.3. Real-time Gameplay & Combat**

Combat is fast-paced and relies on precise rendering on the JavaFX Canvas.

* **Player Control:** WASD movement, defensive guarding, melee attacks, and ranged **Projectile** casting (e.g., Fireball/Rock).  
* **Mechanics:** Features include knockback, invincibility frames after being hit, and particle effects for spells and impacts.  
* **Enemy AI:** Monsters utilize **A\*** pathfinding for sophisticated behaviors (wander, chase, melee, ranged).

### **3.4. World and Environment**

The game world is built on a tile-based system with dynamic environmental effects.

* **Interactivity:** Includes collisions, destructible objects (dry trees, walls), and interactive elements like metal plates.  
* **Puzzles:** A notable puzzle involves pushing a **“Big Rock”** onto metal plates to trigger the opening of an **Iron Door**.  
* **Dynamic Lighting:** Features a full **day/night cycle** (day → dusk → night → dawn) and player-controlled light sources (Lantern).

### **3.5. Inventory, Trading, and Items**

A modular UI manages player items and economics.

* **Items:** Equipment (swords, shields), consumables (potions, tent), and pickups (coins, crystals).  
* **UI:** Dedicated screens for **Inventory** (with tooltips) and **Merchant Trade** (buy/sell).

### **3.6. Cutscenes and Boss Fight**

Narrative elements are delivered through dedicated cutscenes.

* **Boss Introduction:** An intro cutscene seals the arena before the two-phase **Skeleton Lord** boss fight.  
* **Ending:** A concluding cutscene after the boss fight, featuring the **Blue Heart**.

### **3.7. Save/Load and Accounts**

User identity and progress are maintained through persistence features.

* **Authentication:** **Login/Signup** process includes an **OTP email** verification step.  
* **Persistence:** Per-user saves capture the player's stats, inventory, equipment, position, map/area, and global progress flags.

## **4\. Technical Challenges and Solutions**

| Challenge | Solution |
| :---- | :---- |
| **Migrating Rendering to JavaFX Canvas** | Rewrote drawing using **GraphicsContext** and **Image** objects. Implemented lighting by pre-rendering light sources to a **WritableImage** snapshot and utilized global alpha for visual effects (fades, invincibility). |
| **Reliable LAN Multiplayer Sync** | Implemented immediate position broadcasts from clients and utilized server-authoritative **snapshot sync** for monsters and object states. Ensured clear removal and broadcast events for entity lifecycle. |
| **Pathfinding Stability and Bounds** | Fixed subtle bugs in the A\* implementation (specifically, using \< instead of \<=). Ensured node states were **reset** for each search and added guards for reading the next step from empty paths. |
| **Robust Save/Load Across Maps** | Utilized **ragged arrays** sized per map. Employed **try-with-resources** for secure I/O streams and meticulously saved player-specific data (map, area, position, direction) alongside global progress flags. |

## **5\. Resources and Appendix**

| Resource Category | Key Technologies/References |
| :---- | :---- |
| **Framework** | OpenJFX Documentation (Canvas, Images, Compositing). |
| **Core Java** | Oracle Java Tutorials (Sockets, I/O, Threading, Serialization). |
| **AI/Algorithms** | A\* Pathfinding references (Manhattan heuristic, f/g/h costs, open/closed sets). |
| **Authentication** | **Jakarta Mail** (SMTP with STARTTLS). |

### **Key Class Summary**

| Class Name | Functionality |
| :---- | :---- |
| **GamePanel** | Manages the main game loop, state machine, rendering, and multiplayer synchronization logic. |
| **Entity** | The base class for all dynamic world actors and items. |
| **Player/NPCs/Monsters** | Implement specific controls, AI behaviors, combat, and loot drops. |
| **GameServer/ClientHandler/GameClient** | The core LAN networking stack. |
| **TileManager/Map** | Handles tile loading, drawing, and mini/full map generation. |
| **UI** | Manages all on-screen UI components, including inventory, dialogue, and chat interfaces. |
| **PathFinder** | The implementation of the A\* pathfinding algorithm with Node grid cost management. |
| **SaveLoad/DataStorage** | Handles per-user data persistence and serialization. |

### **How to Run (LAN)**

1. **Host:** From the Title screen, navigate to **Multiplayer** → **Host Game**. Copy the generated Join Code (e.g., 192.168.1.104:7777) and press **Start Game**.  
2. **Guest:** From the Title screen, navigate to **Multiplayer** → **Join Game**. Paste the complete host:port into the field and press **Connect**.  
3. **Requirement:** Both devices must be on the **same Wi-Fi network**, and firewall permissions must be granted if prompted.

Project Link: \[Insert link\]  
Demo Video: \[Insert link\]
