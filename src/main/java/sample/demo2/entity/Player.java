package sample.demo2.entity;

import sample.demo2.main.GamePanel;
import sample.demo2.main.KeyHandler;
import sample.demo2.object.*;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.awt.Rectangle;

public class Player extends Entity{

    KeyHandler keyH;
    public final int screenX;
    public final int screenY;
    int standCounter = 0;
    public boolean attackCanceled = false;
    public boolean lightUpdated = false;

    public Player(GamePanel gp, KeyHandler keyH)
    {
        super(gp);
        this.gp=gp;
        this.keyH=keyH;

        screenX = gp.screenWidth/2 - (gp.tileSize/2);
        screenY = gp.screenHeight/2- (gp.tileSize/2);

        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 32;
        solidAreaDefaultX = 8;
        solidAreaDefaultY = 16;

        setDefaultValues();
    }

    public void setDefaultValues()
    {
        worldX = gp.tileSize * 23;
        worldY = gp.tileSize * 21;
        gp.currentMap = 0;
        gp.currentArea = gp.outside;
        defaultSpeed = 2;
        speed = defaultSpeed;
        direction = "down";

        //PLAYER STATUS
        level = 1;
        maxLife = 10;
        life = maxLife;
        maxMana = 8;
        mana = maxMana;
        ammo = 10;
        strength = 1;
        dexterity = 1;
        exp = 0;
        nextLevelExp = 4;
        coin = 40;
        invincible = false;
        currentWeapon = new OBJ_Sword_Normal(gp);
        currentShield = new OBJ_Shield_Wood(gp);
        currentLight = null;
        projectile = new OBJ_Fireball(gp);
        attack = getAttack();
        defense = getDefense();

        getImage();
        getAttackImage();
        getGuardImage();
        setItems();
    }
    public void setDefaultPositions()
    {
        gp.currentMap = 0;
        worldX = gp.tileSize * 23;
        worldY = gp.tileSize * 21;
        direction = "down";
    }
    public void setDialogue()
    {
        dialogues[0][0] = "You are level " + level + " now!\n" + "You feel stronger!";
    }
    public void restoreStatus()
    {
        life = maxLife;
        mana = maxMana;
        speed = defaultSpeed;
        invincible = false;
        transparent = false;
        attacking = false;
        guarding = false;
        knockBack = false;
        lightUpdated = true;
    }

    public void setItems()
    {
        inventory.clear();
        inventory.add(currentWeapon);
        inventory.add(currentShield);
    }

    public int getAttack()
    {
        attackArea = currentWeapon.attackArea;
        motion1_duration = currentWeapon.motion1_duration;
        motion2_duration = currentWeapon.motion2_duration;
        return attack = strength * currentWeapon.attackValue;
    }

    public int getDefense()
    {
        return defense = dexterity * currentShield.defenseValue;
    }
    public int getCurrentWeaponSlot()
    {
        int currentWeaponSlot = 0;
        for(int i = 0; i < inventory.size(); i++)
        {
            if(inventory.get(i) == currentWeapon)
            {
                currentWeaponSlot = i;
            }
        }
        return currentWeaponSlot;
    }
    public int getCurrentShieldSlot()
    {
        int currentShieldSlot = 0;
        for(int i = 0; i < inventory.size(); i++)
        {
            if(inventory.get(i) == currentShield)
            {
                currentShieldSlot = i;
            }
        }
        return currentShieldSlot;
    }

    public void getImage()
    {
        up1 = setup("/player/boy_up_1",gp.tileSize,gp.tileSize);
        up2 = setup("/player/boy_up_2",gp.tileSize,gp.tileSize);
        down1 = setup("/player/boy_down_1",gp.tileSize,gp.tileSize);
        down2 = setup("/player/boy_down_2",gp.tileSize,gp.tileSize);
        left1 = setup("/player/boy_left_1",gp.tileSize,gp.tileSize);
        left2 = setup("/player/boy_left_2",gp.tileSize,gp.tileSize);
        right1 = setup("/player/boy_right_1",gp.tileSize,gp.tileSize);
        right2 = setup("/player/boy_right_2",gp.tileSize,gp.tileSize);
    }

    public void getSleepingImage(Image image)
    {
        up1 = image;
        up2 = image;
        down1 = image;
        down2 = image;
        left1 = image;
        left2 = image;
        right1 = image;
        right2 = image;
    }
    public void getAttackImage()
    {
        if(currentWeapon.type == type_sword)
        {
            attackUp1 = setup("/player/boy_attack_up_1",gp.tileSize, gp.tileSize * 2);
            attackUp2 = setup("/player/boy_attack_up_2",gp.tileSize, gp.tileSize * 2);
            attackDown1 = setup("/player/boy_attack_down_1",gp.tileSize, gp.tileSize * 2);
            attackDown2 = setup("/player/boy_attack_down_2",gp.tileSize, gp.tileSize * 2);
            attackLeft1 = setup("/player/boy_attack_left_1",gp.tileSize * 2, gp.tileSize);
            attackLeft2 = setup("/player/boy_attack_left_2",gp.tileSize * 2, gp.tileSize);
            attackRight1 = setup("/player/boy_attack_right_1",gp.tileSize * 2, gp.tileSize);
            attackRight2 = setup("/player/boy_attack_right_2",gp.tileSize * 2, gp.tileSize);
        }
        else if(currentWeapon.type == type_axe)
        {
            attackUp1 = setup("/player/boy_axe_up_1",gp.tileSize, gp.tileSize * 2);
            attackUp2 = setup("/player/boy_axe_up_2",gp.tileSize, gp.tileSize * 2);
            attackDown1 = setup("/player/boy_axe_down_1",gp.tileSize, gp.tileSize * 2);
            attackDown2 = setup("/player/boy_axe_down_2",gp.tileSize, gp.tileSize * 2);
            attackLeft1 = setup("/player/boy_axe_left_1",gp.tileSize * 2, gp.tileSize);
            attackLeft2 = setup("/player/boy_axe_left_2",gp.tileSize * 2, gp.tileSize);
            attackRight1 = setup("/player/boy_axe_right_1",gp.tileSize * 2, gp.tileSize);
            attackRight2 = setup("/player/boy_axe_right_2",gp.tileSize * 2, gp.tileSize);
        }
        else if(currentWeapon.type == type_pickaxe)
        {
            attackUp1 = setup("/player/boy_pick_up_1",gp.tileSize, gp.tileSize * 2);
            attackUp2 = setup("/player/boy_pick_up_2",gp.tileSize, gp.tileSize * 2);
            attackDown1 = setup("/player/boy_pick_down_1",gp.tileSize, gp.tileSize * 2);
            attackDown2 = setup("/player/boy_pick_down_2",gp.tileSize, gp.tileSize * 2);
            attackLeft1 = setup("/player/boy_pick_left_1",gp.tileSize * 2, gp.tileSize);
            attackLeft2 = setup("/player/boy_pick_left_2",gp.tileSize * 2, gp.tileSize);
            attackRight1 = setup("/player/boy_pick_right_1",gp.tileSize * 2, gp.tileSize);
            attackRight2 = setup("/player/boy_pick_right_2",gp.tileSize * 2, gp.tileSize);
        }
    }
    public void getGuardImage()
    {
        guardUp = setup("/player/boy_guard_up",gp.tileSize,gp.tileSize);
        guardDown = setup("/player/boy_guard_down",gp.tileSize,gp.tileSize);
        guardLeft = setup("/player/boy_guard_left",gp.tileSize,gp.tileSize);
        guardRight = setup("/player/boy_guard_right",gp.tileSize,gp.tileSize);
    }

    // --- MODIFIED --- This method now sends a death notification in multiplayer.
    public void update()
    {
        if(knockBack)
        {
            collisionOn = false;
            gp.cChecker.checkTile(this);
            gp.cChecker.checkObject(this,true);
            gp.cChecker.checkEntity(this, gp.npc);
            gp.cChecker.checkEntity(this, gp.monster);
            gp.cChecker.checkEntity(this, gp.iTile);
            if(collisionOn) {
                knockBackCounter = 0;
                knockBack = false;
                speed = defaultSpeed;
            } else {
                switch (knockBackDirection) {
                    case "up" : worldY -= speed; break;
                    case "down" : worldY += speed; break;
                    case "left" : worldX -= speed; break;
                    case "right" : worldX += speed; break;
                }
            }
            knockBackCounter++;
            if(knockBackCounter == 10) {
                knockBackCounter = 0;
                knockBack = false;
                speed = defaultSpeed;
            }
        }
        else if(attacking)
        {
            attacking();
        }
        else if (keyH != null)
        {
            if(keyH.spacePressed) {
                guarding = true;
                guardCounter++;
            } else if(keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed || keyH.enterPressed) {
                if(keyH.upPressed) { direction = "up"; }
                else if(keyH.downPressed) { direction = "down"; }
                else if(keyH.leftPressed) { direction = "left"; }
                else if(keyH.rightPressed) { direction = "right"; }
                collisionOn = false;
                gp.cChecker.checkTile(this);
                int objIndex = gp.cChecker.checkObject(this,true);
                pickUpObject(objIndex);
                int npcIndex = gp.cChecker.checkEntity(this, gp.npc);
                interactNPC(npcIndex);
                int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
                contactMonster(monsterIndex);
                gp.cChecker.checkEntity(this, gp.iTile);
                gp.eHandler.checkEvent();
                if(!collisionOn && !keyH.enterPressed) {
                    switch (direction) {
                        case "up" : worldY -= speed; break;
                        case "down" : worldY += speed; break;
                        case "left" : worldX -= speed; break;
                        case "right" : worldX += speed; break;
                    }
                }
                if(keyH.enterPressed && !attackCanceled) {
                    if (gp.gameClient != null) {
                        gp.gameClient.sendData("ATK:" + direction);
                    } else {
                        gp.playSE(7);
                        attacking = true;
                        spriteCounter = 0;
                    }
                    attackCanceled = true;
                }
                attackCanceled = false;
                gp.keyH.enterPressed = false;
                guarding = false;
                guardCounter = 0;
                spriteCounter++;
                if (spriteCounter > 12) {
                    if (spriteNum == 1) { spriteNum = 2; }
                    else if (spriteNum == 2) { spriteNum = 1; }
                    spriteCounter = 0;
                }
            } else {
                standCounter++;
                if(standCounter == 20) {
                    spriteNum = 1;
                    standCounter = 0;
                }
                guarding = false;
                guardCounter = 0;
            }
            // Inside Player.update(), replace your shooting block with this:
            if (keyH.shotKeyPressed && !projectile.alive && shotAvailableCounter == 30 && projectile.haveResource(this)) {
                String projName = projectile.name; // e.g., "Fireball" or "Rock"

                if (gp.gameClient != null) {
                    // If we are a pure client (guest), spawn locally so we see it immediately.
                    // The server will broadcast to others, excluding us, so no duplicate.
                    if (gp.gameServer == null) {
                        gp.spawnProjectile(projName, worldX, worldY, direction, this);
                    }

                    // Tell the server to spawn and broadcast to the other clients
                    gp.gameClient.sendData("PROJ:" + projName + ":" + worldX + ":" + worldY + ":" + direction);

                    // Spend resource and cooldown locally
                    projectile.subtractResource(this);
                    shotAvailableCounter = 0;
                    gp.playSE(10);
                } else {
                    // Single-player fallback
                    projectile.set(worldX, worldY, direction, true, this);
                    for (int i = 0; i < gp.projectile[1].length; i++) {
                        if (gp.projectile[gp.currentMap][i] == null) {
                            gp.projectile[gp.currentMap][i] = projectile;
                            break;
                        }
                    }
                    projectile.subtractResource(this);
                    shotAvailableCounter = 0;
                    gp.playSE(10);
                }
            }
        }

        if(invincible) {
            invincibleCounter++;
            if(invincibleCounter > 60) {
                invincible = false;
                transparent = false;
                invincibleCounter = 0;
            }
        }
        if(shotAvailableCounter < 30) { shotAvailableCounter++; }
        if(life > maxLife) { life = maxLife; }
        if(mana > maxMana) { mana = maxMana; }

        boolean isGodMode = (keyH != null && keyH.godModeOn);
        if (!isGodMode && life <= 0) {
            // --- NEW --- If in multiplayer, notify the server of your death.
            if (gp.gameClient != null) {
                gp.gameClient.sendData("PLAYER_DIED");
            }
            // Standard game over logic
            gp.gameState = gp.gameOverState;
            gp.ui.commandNum =- 1;
            gp.stopMusic();
            gp.playSE(12);
        }
    }

    // ... keep the rest of your Player class unchanged ...
// Replace your pickUpObject method with this:

    public void pickUpObject(int i)
    {
        if(i != 999) {
            if(gp.obj[gp.currentMap][i].type == type_pickupOnly) {
                gp.obj[gp.currentMap][i].use(this);
                gp.obj[gp.currentMap][i] = null;
                if (gp.gameClient != null) {
                    gp.gameClient.sendData("OBJ_PICKUP:" + i);
                }
            }
            else if(gp.obj[gp.currentMap][i].type == type_obstacle) {
                if(keyH.enterPressed) {
                    attackCanceled = true;
                    gp.obj[gp.currentMap][i].interact();
                }
            }
            else {
                String text;
                if(canObtainItem(gp.obj[gp.currentMap][i])) {
                    gp.playSE(1);
                    text = "Got a " + gp.obj[gp.currentMap][i].name + "!";
                    gp.obj[gp.currentMap][i] = null;
                    if (gp.gameClient != null) {
                        gp.gameClient.sendData("OBJ_PICKUP:" + i);
                    }
                } else {
                    text = "You cannot carry any more";
                }
                gp.ui.addMessage(text);
            }
        }
    }
    public void interactNPC(int i)
    {
        if(i != 999) {
            if(gp.keyH.enterPressed) {
                attackCanceled = true;
                gp.npc[gp.currentMap][i].speak();
            }
            gp.npc[gp.currentMap][i].move(direction);
        }
    }
    public void contactMonster(int i)
    {
        if(i != 999) {
            if(!invincible && !gp.monster[gp.currentMap][i].dying) {
                gp.playSE(6);
                int damage = gp.monster[gp.currentMap][i].attack - defense;
                if(damage < 1) { damage = 1; }
                life -= damage;
                invincible = true;
                transparent = true;
            }
        }
    }

    public void damageMonster(int i, Entity attacker, int attack, int knockBackPower) {
        if (i != 999) {
            applyMonsterDamage(i, attacker, attack, knockBackPower);
        }
    }

    public void applyMonsterDamage(int i, Entity attacker, int attack, int knockBackPower) {
        if (i != 999 && gp.monster[gp.currentMap][i] != null && !gp.monster[gp.currentMap][i].invincible) {
            gp.playSE(5);

            if (knockBackPower > 0) {
                setKnockBack(gp.monster[gp.currentMap][i], attacker, knockBackPower);
            }
            if (gp.monster[gp.currentMap][i].offBalance) {
                attack *= 2;
            }
            int damage = attack - gp.monster[gp.currentMap][i].defense;
            if (damage <= 0) {
                damage = 1;
            }
            gp.monster[gp.currentMap][i].life -= damage;
            gp.ui.addMessage(damage + " damage!");
            gp.monster[gp.currentMap][i].invincible = true;
            gp.monster[gp.currentMap][i].damageReaction();

            if (gp.monster[gp.currentMap][i].life <= 0) {
                gp.monster[gp.currentMap][i].dying = true;
                gp.ui.addMessage("Killed the " + gp.monster[gp.currentMap][i].name + "!");
                gp.ui.addMessage("Exp +" + gp.monster[gp.currentMap][i].exp + "!");
                exp += gp.monster[gp.currentMap][i].exp;
                checkLevelUp();
            }
        }
    }
    public void damageInteractiveTile(int i)
    {
        if(i != 999 && gp.iTile[gp.currentMap][i].destructible && gp.iTile[gp.currentMap][i].isCorrectItem(this) && !gp.iTile[gp.currentMap][i].invincible) {
            gp.iTile[gp.currentMap][i].playSE();
            gp.iTile[gp.currentMap][i].life--;
            gp.iTile[gp.currentMap][i].invincible = true;
            generateParticle(gp.iTile[gp.currentMap][i], gp.iTile[gp.currentMap][i]);
            if(gp.iTile[gp.currentMap][i].life == 0) {
                gp.iTile[gp.currentMap][i] = gp.iTile[gp.currentMap][i].getDestroyedForm();
            }
        }
    }
    public void damageProjectile(int i)
    {
        if(i != 999) {
            Entity projectile = gp.projectile[gp.currentMap][i];
            projectile.alive = false;
            generateParticle(projectile,projectile);
        }
    }
    public void checkLevelUp()
    {
        while(exp >= nextLevelExp) {
            level++;
            exp = exp - nextLevelExp;
            if(level <= 4) {
                nextLevelExp = nextLevelExp + 4;
            } else {
                nextLevelExp = nextLevelExp + 8;
            }
            maxLife += 2;
            strength++;
            dexterity++;
            attack = getAttack();
            defense = getDefense();
            gp.playSE(8);
            dialogues[0][0] = "You are level " + level + " now!\n" + "You feel stronger!";
            setDialogue();
            startDialogue(this,0);
        }
    }
    public void selectItem() {
        int itemIndex = gp.ui.getItemIndexOnSlot(gp.ui.playerSlotCol, gp.ui.playerSlotRow);
        if (itemIndex < inventory.size()) {
            Entity selectedItem = inventory.get(itemIndex);
            boolean equipmentChanged = false;

            if (selectedItem.type == type_sword || selectedItem.type == type_axe || selectedItem.type == type_pickaxe) {
                currentWeapon = selectedItem;
                attack = getAttack();
                getAttackImage();
                equipmentChanged = true;
            }
            if (selectedItem.type == type_shield) {
                currentShield = selectedItem;
                defense = getDefense();
                equipmentChanged = true;
            }
            if (selectedItem.type == type_light) {
                if (currentLight == selectedItem) currentLight = null;
                else currentLight = selectedItem;
                lightUpdated = true;
                equipmentChanged = true;
            }
            if (selectedItem.type == type_consumable) {
                if (selectedItem.use(this)) {
                    if (selectedItem.amount > 1) selectedItem.amount--;
                    else inventory.remove(itemIndex);
                }
            }

            if (equipmentChanged && gp.gameClient != null) {
                String w = (currentWeapon != null) ? currentWeapon.name : "NA";
                String s = (currentShield != null) ? currentShield.name : "NA";
                String l = (currentLight  != null) ? currentLight.name  : "NA";
                gp.gameClient.sendData("EQUIP:" + w + ":" + s + ":" + l);
            }
        }
    }
    public int searchItemInInventory(String itemName)
    {
        int itemIndex = 999;
        for(int i = 0; i < inventory.size(); i++) {
            if(inventory.get(i).name.equals(itemName)) {
                itemIndex = i;
                break;
            }
        }
        return itemIndex;
    }
    public boolean canObtainItem(Entity item)
    {
        boolean canObtain = false;
        Entity newItem = gp.eGenerator.getObject(item.name);
        if(newItem.stackable) {
            int index = searchItemInInventory(newItem.name);
            if(index != 999) {
                inventory.get(index).amount++;
                canObtain = true;
            } else {
                if(inventory.size() != maxInventorySize) {
                    inventory.add(newItem);
                    canObtain = true;
                }
            }
        } else {
            if(inventory.size() != maxInventorySize) {
                inventory.add(newItem);
                canObtain = true;
            }
        }
        return canObtain;
    }

    // --- MODIFIED --- This method now draws the player's name.
    @Override
    public void draw(GraphicsContext gc) {
        Image image = null;
        int finalScreenX, finalScreenY;

        if (this == gp.player) {
            finalScreenX = screenX;
            finalScreenY = screenY;
        } else {
            finalScreenX = worldX - gp.player.worldX + gp.player.screenX;
            finalScreenY = worldY - gp.player.worldY + gp.player.screenY;

            if (worldX + gp.tileSize < gp.player.worldX - gp.player.screenX ||
                    worldX - gp.tileSize > gp.player.worldX + gp.player.screenX ||
                    worldY + gp.tileSize < gp.player.worldY - gp.player.screenY ||
                    worldY - gp.tileSize > gp.player.worldY + gp.player.screenY) {
                return;
            }
        }

        int tempScreenX = finalScreenX;
        int tempScreenY = finalScreenY;

        switch (direction) {
            case "up":
                if (!attacking) {
                    image = (spriteNum == 1) ? up1 : up2;
                } else {
                    tempScreenY = finalScreenY - gp.tileSize;
                    image = (spriteNum == 1) ? attackUp1 : attackUp2;
                }
                if (guarding) image = guardUp;
                break;
            case "down":
                if (!attacking) {
                    image = (spriteNum == 1) ? down1 : down2;
                } else {
                    image = (spriteNum == 1) ? attackDown1 : attackDown2;
                }
                if (guarding) image = guardDown;
                break;
            case "left":
                if (!attacking) {
                    image = (spriteNum == 1) ? left1 : left2;
                } else {
                    tempScreenX = finalScreenX - gp.tileSize;
                    image = (spriteNum == 1) ? attackLeft1 : attackLeft2;
                }
                if (guarding) image = guardLeft;
                break;
            case "right":
                if (!attacking) {
                    image = (spriteNum == 1) ? right1 : right2;
                } else {
                    image = (spriteNum == 1) ? attackRight1 : attackRight2;
                }
                if (guarding) image = guardRight;
                break;
        }

        double originalAlpha = gc.getGlobalAlpha();
        if (transparent) {
            gc.setGlobalAlpha(0.4);
        }

        if (image != null) {
            gc.drawImage(image, tempScreenX, tempScreenY);
        }

        gc.setGlobalAlpha(originalAlpha);

        // --- NEW --- Draw Player Name above the character
        if (name != null) {
            gc.setFont(Font.font("Arial", 16));
            gc.setFill(Color.WHITE);

            Text theText = new Text(name);
            theText.setFont(gc.getFont());
            double textWidth = theText.getLayoutBounds().getWidth();
            double x = finalScreenX + (gp.tileSize / 2.0) - (textWidth / 2.0);
            double y = finalScreenY - 5;

            // Draw a semi-transparent background for readability
            gc.setFill(Color.rgb(0, 0, 0, 0.5));
            gc.fillRoundRect(x - 5, y - 18, textWidth + 10, 22, 10, 10);

            gc.setFill(Color.WHITE);
            gc.fillText(name, x, y);
        }
    }


    public void respawn() {
        // Reset player's core stats
        life = maxLife;
        mana = maxMana;
        invincible = false;
        transparent = false;
        attacking = false;
        guarding = false;
        knockBack = false;
        speed = defaultSpeed; // Just in case speed was altered (e.g., by knockback)

        // Reset player's position to the default starting point
        setDefaultPositions();

        // --- CRITICAL FOR MULTIPLAYER ---
        // Immediately send the new position to the server so other players see the respawn.
        if (gp.gameClient != null) {
            String respawnData = "POS:" + worldX + ":" + worldY + ":" + direction;
            gp.gameClient.sendData(respawnData);
        }
    }


}