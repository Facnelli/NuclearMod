package nuclearmod.type.bullet;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.gen.Bullet;
import nuclearmod.config.ModBalance;
import nuclearmod.config.ModBalanceDefaults;
import nuclearmod.content.ModFx;
import nuclearmod.mechanics.nuke.NuclearExplosion;

public class NukeBulletType extends ArtilleryBulletType {
    public NukeBulletType() {
        super(ModBalanceDefaults.Nuclear.SHELL_SPEED, ModBalanceDefaults.Nuclear.SHELL_DAMAGE);
        hitEffect = ModFx.nukeExplosion;
        despawnEffect = ModFx.nukeExplosion;
        sprite = "shell";
        lifetime = ModBalanceDefaults.Nuclear.SHELL_LIFETIME_FRAMES;
        width = 14f;
        height = 18f;
        trailWidth = 6f;
        trailLength = 35;
        trailColor = Color.valueOf("ff7a38");
        trailEffect = Fx.missileTrailSmoke;
        trailInterval = 2f;
        trailParam = 4f;
        collides = true;
        collidesAir = true;
        collidesGround = false;
        collidesTeam = true;
        hitShake = ModBalanceDefaults.Nuclear.HIT_SHAKE;
    }

    @Override
    public void hit(Bullet bullet, float x, float y) {
        speed = ModBalance.Nuclear.shellSpeed;
        damage = ModBalance.Nuclear.shellDamage;
        lifetime = ModBalance.Nuclear.shellLifetimeFrames;
        hitShake = ModBalance.Nuclear.hitShake;
        super.hit(bullet, x, y);
        NuclearExplosion.detonate(bullet, x, y);
    }

    @Override
    public void despawned(Bullet bullet) {
        speed = ModBalance.Nuclear.shellSpeed;
        damage = ModBalance.Nuclear.shellDamage;
        lifetime = ModBalance.Nuclear.shellLifetimeFrames;
        hitShake = ModBalance.Nuclear.hitShake;
        super.despawned(bullet);
        NuclearExplosion.detonate(bullet, bullet.x, bullet.y);
    }
}
