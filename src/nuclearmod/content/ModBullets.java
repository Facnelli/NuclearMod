package nuclearmod.content;

import nuclearmod.type.bullet.BlueFireBulletType;
import nuclearmod.type.bullet.NukeBulletType;
import nuclearmod.type.bullet.NukeSparkBulletType;

public class ModBullets {
    public static BlueFireBulletType blueFire;
    public static NukeSparkBulletType nukeSpark;
    public static NukeBulletType nukeBullet;

    public static void load() {
        blueFire = new BlueFireBulletType();
        nukeSpark = new NukeSparkBulletType(blueFire);
        nukeBullet = new NukeBulletType();
    }
}
