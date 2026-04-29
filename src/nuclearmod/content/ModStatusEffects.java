package nuclearmod.content;

import mindustry.type.StatusEffect;
import nuclearmod.type.status.BlueFireStatusEffect;

public class ModStatusEffects {
    public static StatusEffect blueFireBurn;

    public static void load() {
        blueFireBurn = new BlueFireStatusEffect("blue-fire-burn");
    }
}
