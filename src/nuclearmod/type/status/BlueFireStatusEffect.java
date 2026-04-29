package nuclearmod.type.status;

import arc.graphics.Color;
import arc.math.Mathf;
import mindustry.content.StatusEffects;
import mindustry.entities.units.StatusEntry;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;
import mindustry.world.Tile;
import nuclearmod.config.ModBalance;
import nuclearmod.config.ModBalanceDefaults;
import nuclearmod.content.ModFx;
import nuclearmod.mechanics.bluefire.BlueFireSystem;

public class BlueFireStatusEffect extends StatusEffect {
    public BlueFireStatusEffect(String name) {
        super(name);
        color = Color.valueOf("00aaff");
        damage = ModBalance.BlueFireStatus.damagePerFrame;
        effect = ModFx.blueFire;
        effectChance = ModBalance.BlueFireStatus.effectChancePerFrame;
    }

    @Override
    public void update(Unit unit, StatusEntry entry) {
        damage = ModBalance.BlueFireStatus.damagePerFrame;
        effectChance = ModBalance.BlueFireStatus.effectChancePerFrame;
        super.update(unit, entry);

        if (unit.hasEffect(StatusEffects.wet) || unit.hasEffect(StatusEffects.freezing)) {
            unit.unapply(this);
            return;
        }

        if (Mathf.chanceDelta(ModBalance.BlueFireStatus.trailIgnitionChancePerFrame)) {
            Tile tile = unit.tileOn();
            if (tile != null) {
                BlueFireSystem.spawnOn(tile, unit.team);
            }
        }
    }
}
