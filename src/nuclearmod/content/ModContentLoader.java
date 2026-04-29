package nuclearmod.content;

import nuclearmod.content.blocks.ModCraftingBlocks;
import nuclearmod.content.blocks.ModDefenseBlocks;
import nuclearmod.content.blocks.ModEnvironmentBlocks;
import nuclearmod.content.blocks.ModPayloadBlocks;

public final class ModContentLoader {
    private ModContentLoader() {}

    public static void load() {
        ModItems.load();
        ModFx.load();
        ModStatusEffects.load();
        ModBullets.load();
        ModBlocks.load();
    }

    public static void loadBlocks() {
        ModEnvironmentBlocks.load();
        ModCraftingBlocks.load();
        ModPayloadBlocks.load();
        ModDefenseBlocks.load();
    }
}
