package nuclearmod.type.bullet;

import mindustry.Vars;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.world.Tile;
import nuclearmod.config.ModBalance;
import nuclearmod.config.ModBalanceDefaults;
import nuclearmod.mechanics.bluefire.BlueFireSystem;

public class BlueFireBulletType extends BulletType {
    public BlueFireBulletType() {
        super(ModBalanceDefaults.BlueFire.BULLET_SPEED, ModBalanceDefaults.BlueFire.BULLET_DAMAGE);
        lifetime = ModBalanceDefaults.BlueFire.BULLET_LIFETIME_FRAMES;
        pierce = true;
        pierceBuilding = true;
        collides = false;
        hittable = false;
        absorbable = false;
        drawSize = 0f;
    }

    @Override
    public void draw(Bullet bullet) {}

    @Override
    public void update(Bullet bullet) {
        lifetime = ModBalance.BlueFire.bulletLifetimeFrames;
        super.update(bullet);

        Tile tile = Vars.world.tileWorld(bullet.x, bullet.y);
        if (tile == null || tile.build == null) {
            bullet.remove();
            return;
        }

        if (BlueFireSystem.shouldExtinguish(tile)) {
            bullet.remove();
            return;
        }

        BlueFireSystem.updateVisuals(tile);
        BlueFireSystem.damageBuilding(tile);
        BlueFireSystem.affectUnits(bullet);
        BlueFireSystem.spreadToNearbyBuildings(bullet, tile);
    }
}
