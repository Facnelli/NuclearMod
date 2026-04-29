package nuclearmod.content.blocks;

import mindustry.world.Block;
import mindustry.world.blocks.environment.OreBlock;
import nuclearmod.content.ModItems;

public final class ModEnvironmentBlocks {
    public static Block oreIron;
    public static Block oreUranium;

    private ModEnvironmentBlocks() {}

    public static void load() {
        oreIron = new OreBlock("ore-iron", ModItems.iron) {{
            localizedName = "Minerio de ferro";
            oreDefault = true;
            oreScale = 30.0f;
            oreThreshold = 0.4f;
            variants = 1;
        }};

        oreUranium = new OreBlock("ore-uranium", ModItems.uranio) {{
            localizedName = "Minerio de Uranio";
            oreDefault = true;
            oreScale = 24.0f;
            oreThreshold = 0.88f;
            variants = 1;
        }};
    }
}
