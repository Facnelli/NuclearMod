package nuclearmod.mechanics.nuke;

import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.world.Tile;
import nuclearmod.blocks.defense.LinearShieldProjector;
import nuclearmod.config.ModBalance;
import nuclearmod.config.ModBalanceDefaults;
import nuclearmod.content.ModBullets;
import nuclearmod.content.ModFx;

public final class NuclearExplosion {
    private NuclearExplosion() {}

    public static void detonate(Bullet bullet, float explosionX, float explosionY) {
        ModFx.nukeExplosion.at(explosionX, explosionY);
        Effect.shake(ModBalance.Nuclear.screenShakeIntensity, ModBalance.Nuclear.screenShakeIntensity, explosionX, explosionY);

        int originTileX = Math.round(explosionX / Vars.tilesize);
        int originTileY = Math.round(explosionY / Vars.tilesize);
        int blastRadiusTiles = ModBalance.Nuclear.blastRadiusTiles;

        Seq<ShieldSegment> activeSegments = collectActiveSegments();
        Seq<Building> criticalTargets = new Seq<>();
        Seq<Building> severeTargets = new Seq<>();

        int tileX = (int)(explosionX / Vars.tilesize);
        int tileY = (int)(explosionY / Vars.tilesize);

        for (int dx = -blastRadiusTiles; dx <= blastRadiusTiles; dx++) {
            for (int dy = -blastRadiusTiles; dy <= blastRadiusTiles; dy++) {
                float distanceTiles = Mathf.dst(0, 0, dx, dy);
                if (distanceTiles > blastRadiusTiles) continue;

                Tile tile = Vars.world.tile(tileX + dx, tileY + dy);
                if (tile == null || isShielded(explosionX, explosionY, originTileX, originTileY, tile, activeSegments)) continue;

                if (tile.build != null) {
                    if (distanceTiles <= ModBalance.Nuclear.criticalBuildingRadiusTiles) criticalTargets.addUnique(tile.build);
                    else severeTargets.addUnique(tile.build);
                }

                if (dx % 2 == 0 && dy % 2 == 0) {
                    ModFx.nukeScorch.at(tile.worldx(), tile.worldy());
                }

                if (distanceTiles > ModBalance.Nuclear.fireRingMinRadiusTiles
                    && Mathf.chance(ModBalance.Nuclear.fireRingChance)) {
                    mindustry.entities.Puddles.deposit(tile, mindustry.content.Liquids.oil, 10000f);
                    mindustry.entities.Fires.create(tile);
                }
            }
        }

        for (Building target : criticalTargets) target.damage(ModBalance.Nuclear.criticalBuildingDamage);
        for (Building target : severeTargets) target.damage(ModBalance.Nuclear.severeBuildingDamage);

        damageUnits(bullet, explosionX, explosionY, originTileX, originTileY, blastRadiusTiles, activeSegments);

        for (int i = 0; i < ModBalance.Nuclear.sparkCount; i++) {
            float angle = Mathf.random(360f);
            float lifeScale = Mathf.random(0.7f, 1.3f);
            Call.createBullet(ModBullets.nukeSpark, bullet.team, explosionX, explosionY, angle, 1f, lifeScale, 1f);
        }
    }

    private static Seq<ShieldSegment> collectActiveSegments() {
        Seq<ShieldSegment> activeSegments = new Seq<>();
        for (LinearShieldProjector.LinearShieldBuild shield : LinearShieldProjector.activeShields) {
            if (shield.broken || shield.warmup <= ModBalance.Shield.activeWarmupThreshold) continue;

            for (int i = 0; i < shield.links.size; i++) {
                Building linked = Vars.world.build(shield.links.get(i));
                if (linked instanceof LinearShieldProjector.LinearShieldBuild other && linked.pos() > shield.pos()) {
                    if (other.broken || other.warmup <= ModBalance.Shield.activeWarmupThreshold) continue;
                    activeSegments.add(new ShieldSegment(shield, other));
                }
            }
        }
        return activeSegments;
    }

    private static boolean isShielded(float explosionX, float explosionY, int originTileX, int originTileY, Tile target,
                                      Seq<ShieldSegment> activeSegments) {
        for (ShieldSegment segment : activeSegments) {
            if (Intersector.intersectSegments(explosionX, explosionY, target.worldx(), target.worldy(),
            segment.first.x, segment.first.y, segment.second.x, segment.second.y, null)) {
                return true;
            }
        }

        int targetTileX = target.x;
        int targetTileY = target.y;
        return Vars.world.raycast(originTileX, originTileY, targetTileX, targetTileY, (rayX, rayY) -> {
            if (rayX == targetTileX && rayY == targetTileY) return false;

            Tile checkTile = Vars.world.tile(rayX, rayY);
            return checkTile != null && checkTile.solid() && checkTile.build == null;
        });
    }

    private static void damageUnits(Bullet bullet, float explosionX, float explosionY, int originTileX, int originTileY,
                                    int blastRadiusTiles, Seq<ShieldSegment> activeSegments) {
        float blastRadiusPixels = blastRadiusTiles * Vars.tilesize;
        Units.nearby(explosionX - blastRadiusPixels, explosionY - blastRadiusPixels, blastRadiusPixels * 2f, blastRadiusPixels * 2f, unit -> {
            boolean unitShielded = false;

            for (ShieldSegment segment : activeSegments) {
                if (Intersector.intersectSegments(explosionX, explosionY, unit.x, unit.y,
                segment.first.x, segment.first.y, segment.second.x, segment.second.y, null)) {
                    unitShielded = true;
                    break;
                }
            }

            if (!unitShielded) {
                int targetTileX = Math.round(unit.x / Vars.tilesize);
                int targetTileY = Math.round(unit.y / Vars.tilesize);

                unitShielded = Vars.world.raycast(originTileX, originTileY, targetTileX, targetTileY, (rayX, rayY) -> {
                    if (rayX == targetTileX && rayY == targetTileY) return false;

                    Tile checkTile = Vars.world.tile(rayX, rayY);
                    return checkTile != null && checkTile.solid() && checkTile.build == null;
                });
            }

            if (!unitShielded) {
                float distance = unit.dst(explosionX, explosionY) / Vars.tilesize;
                if (distance <= ModBalance.Nuclear.unitInstantKillRadiusTiles) {
                    unit.kill();
                } else {
                    unit.damage(ModBalance.Nuclear.unitOuterDamage);
                    unit.apply(StatusEffects.burning, ModBalance.Nuclear.unitBurnDurationFrames);
                }
            }
        });
    }

    private static final class ShieldSegment {
        final LinearShieldProjector.LinearShieldBuild first;
        final LinearShieldProjector.LinearShieldBuild second;

        ShieldSegment(LinearShieldProjector.LinearShieldBuild first, LinearShieldProjector.LinearShieldBuild second) {
            this.first = first;
            this.second = second;
        }
    }
}
