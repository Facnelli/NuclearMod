package nuclearmod.mechanics.bluefire;

import arc.math.Mathf;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.entities.Puddles;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.world.Tile;
import nuclearmod.config.ModBalance;
import nuclearmod.config.ModBalanceDefaults;
import nuclearmod.content.ModBullets;
import nuclearmod.content.ModFx;
import nuclearmod.content.ModStatusEffects;

public final class BlueFireSystem {
    private BlueFireSystem() {}

    public static boolean shouldExtinguish(Tile tile) {
        var puddle = Puddles.get(tile);
        return puddle != null
            && puddle.liquid.temperature <= ModBalance.BlueFire.extinguishMaxLiquidTemperature
            && puddle.amount > ModBalance.BlueFire.extinguishMinPuddleAmount;
    }

    public static boolean hasBlueFireAt(Tile tile) {
        boolean[] infected = {false};
        Groups.bullet.intersect(tile.worldx() - 1f, tile.worldy() - 1f, 2f, 2f, other -> {
            if (other.type == ModBullets.blueFire) {
                infected[0] = true;
            }
        });
        return infected[0];
    }

    public static void spawnOn(Tile tile, Bullet source) {
        if (tile == null || tile.build == null || hasBlueFireAt(tile)) return;
        Call.createBullet(ModBullets.blueFire, source.team, tile.worldx(), tile.worldy(), 0f, 0f, 1f, 1f);
    }

    public static void spawnOn(Tile tile, mindustry.game.Team team) {
        if (tile == null || tile.build == null || hasBlueFireAt(tile)) return;
        Call.createBullet(ModBullets.blueFire, team, tile.worldx(), tile.worldy(), 0f, 0f, 1f, 1f);
    }

    public static void updateVisuals(Tile tile) {
        if (Mathf.chanceDelta(ModBalance.BlueFire.fireCreationChancePerFrame)) {
            mindustry.entities.Fires.create(tile);
        }

        if (Mathf.chanceDelta(ModBalance.BlueFire.visualEffectChancePerFrame)) {
            ModFx.blueFire.at(tile.worldx() + Mathf.range(6f), tile.worldy() + Mathf.range(6f));
        }
    }

    public static void damageBuilding(Tile tile) {
        if (Mathf.chanceDelta(ModBalance.BlueFire.buildingDamageChancePerFrame)) {
            tile.build.damage(tile.build.maxHealth() * ModBalance.BlueFire.buildingDamagePercentPerTrigger);
        }
    }

    public static void affectUnits(Bullet bullet) {
        if (Mathf.chanceDelta(ModBalance.BlueFire.unitEffectChancePerFrame)) {
            float affectRadius = ModBalance.BlueFire.unitAffectRadius;
            mindustry.entities.Units.nearby(bullet.x - affectRadius, bullet.y - affectRadius, affectRadius * 2f, affectRadius * 2f, unit -> {
                if (!unit.hasEffect(StatusEffects.wet) && !unit.hasEffect(StatusEffects.freezing)) {
                    unit.apply(ModStatusEffects.blueFireBurn, ModBalance.BlueFire.unitStatusDurationFrames);
                }
            });
        }
    }

    public static void spreadToNearbyBuildings(Bullet bullet, Tile origin) {
        if (!Mathf.chanceDelta(ModBalance.BlueFire.spreadAttemptChancePerFrame)) return;

        for (int dx = -ModBalance.BlueFire.spreadRadiusTiles; dx <= ModBalance.BlueFire.spreadRadiusTiles; dx++) {
            for (int dy = -ModBalance.BlueFire.spreadRadiusTiles; dy <= ModBalance.BlueFire.spreadRadiusTiles; dy++) {
                if (dx == 0 && dy == 0) continue;

                if (Mathf.chance(ModBalance.BlueFire.spreadNeighborChance)) {
                    Tile neighbor = Vars.world.tile(origin.x + dx, origin.y + dy);
                    if (neighbor != null && neighbor.build != null && !hasBlueFireAt(neighbor)) {
                        Call.createBullet(ModBullets.blueFire, bullet.team, neighbor.worldx(), neighbor.worldy(), 0f, 0f, 1f, 1f);
                    }
                }
            }
        }
    }
}
