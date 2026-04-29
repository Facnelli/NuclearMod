package nuclearmod.type.bullet;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import nuclearmod.config.ModBalance;
import nuclearmod.config.ModBalanceDefaults;

public class NukeSparkBulletType extends BasicBulletType {
    private final BlueFireBulletType blueFireType;

    public NukeSparkBulletType(BlueFireBulletType blueFireType) {
        super(ModBalanceDefaults.NuclearSpark.SPEED, ModBalanceDefaults.NuclearSpark.DAMAGE);
        this.blueFireType = blueFireType;
        lifetime = ModBalanceDefaults.NuclearSpark.LIFETIME_FRAMES;
        trailColor = Color.valueOf("00aaff");
        trailWidth = 3f;
        trailLength = 20;
        frontColor = Color.white;
        backColor = Color.valueOf("0044cc");
        hitEffect = Fx.hitFlameSmall;
        despawnEffect = Fx.none;
        collidesAir = false;
        fragVelocityMin = ModBalanceDefaults.NuclearSpark.FRAG_VELOCITY_MIN;
        fragVelocityMax = ModBalanceDefaults.NuclearSpark.FRAG_VELOCITY_MAX;
    }

    @Override
    public void hitTile(Bullet bullet, Building build, float x, float y, float initialHealth, boolean direct) {
        speed = ModBalance.NuclearSpark.speed;
        damage = ModBalance.NuclearSpark.damage;
        lifetime = ModBalance.NuclearSpark.lifetimeFrames;
        fragVelocityMin = ModBalance.NuclearSpark.fragVelocityMin;
        fragVelocityMax = ModBalance.NuclearSpark.fragVelocityMax;
        super.hitTile(bullet, build, x, y, initialHealth, direct);
        if (build != null) {
            Call.createBullet(blueFireType, bullet.team, build.x, build.y, 0f, 0f, 1f, 1f);
        }
    }

    @Override
    public void hitEntity(Bullet bullet, mindustry.gen.Hitboxc entity, float initialHealth) {
        speed = ModBalance.NuclearSpark.speed;
        damage = ModBalance.NuclearSpark.damage;
        lifetime = ModBalance.NuclearSpark.lifetimeFrames;
        fragVelocityMin = ModBalance.NuclearSpark.fragVelocityMin;
        fragVelocityMax = ModBalance.NuclearSpark.fragVelocityMax;
        super.hitEntity(bullet, entity, initialHealth);
        if (entity instanceof Building build) {
            Call.createBullet(blueFireType, bullet.team, build.x, build.y, 0f, 0f, 1f, 1f);
        }
    }
}
