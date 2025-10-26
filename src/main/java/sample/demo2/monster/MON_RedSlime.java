package sample.demo2.monster;

import sample.demo2.entity.Entity;
import sample.demo2.main.GamePanel;
import sample.demo2.object.OBJ_Rock;

public class MON_RedSlime extends Entity {

    GamePanel gp; // cuz of different package
    public MON_RedSlime(GamePanel gp) {
        super(gp);

        this.gp = gp;

        type = type_monster;
        name = "Red Slime";
        defaultSpeed = 2;
        speed = defaultSpeed;
        maxLife = 8;
        life = maxLife;
        attack = 4;
        defense = 0;
        exp = 4;
        projectile = new OBJ_Rock(gp);

        solidArea.x = 3;
        solidArea.y = 18;
        solidArea.width = 42;
        solidArea.height = 30;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        getImage();
    }

    public void getImage()
    {
        up1 = setup("/monster/redslime_down_1",gp.tileSize,gp.tileSize);
        up2 = setup("/monster/redslime_down_2",gp.tileSize,gp.tileSize);
        down1 = setup("/monster/redslime_down_1",gp.tileSize,gp.tileSize);
        down2 = setup("/monster/redslime_down_2",gp.tileSize,gp.tileSize);
        left1 = setup("/monster/redslime_down_1",gp.tileSize,gp.tileSize);
        left2 = setup("/monster/redslime_down_2",gp.tileSize,gp.tileSize);
        right1 = setup("/monster/redslime_down_1",gp.tileSize,gp.tileSize);
        right2 = setup("/monster/redslime_down_2",gp.tileSize,gp.tileSize);
    }
    public void setAction()
    {
        if(onPath == true)
        {
            checkStopChasingOrNot(gp.player,15,100);
            searchPath(getGoalCol(gp.player), getGoalRow(gp.player));
            checkShootOrNot(200, 30);
        }
        else
        {
            checkStartChasingOrNot(gp.player, 5, 100);
            getRandomDirection(120);
        }
    }

    public void damageReaction() {
        actionLockCounter = 0;
        onPath = true; // gets aggro
    }

    @Override
    public void checkDrop() {
        gp.bossBattleOn = false;
        sample.demo2.data.Progress.skeletonLordDefeated = true;

        gp.stopMusic();
        gp.playMusic(19);

        for(int i = 0; i < gp.obj[1].length; i++) {
            if(gp.obj[gp.currentMap][i] != null && gp.obj[gp.currentMap][i].name.equals(sample.demo2.object.OBJ_Door_Iron.objName)) {
                gp.playSE(21);
                gp.obj[gp.currentMap][i] = null;
            }
        }
        super.checkDrop();
    }


}