package nuclearmod.content;

import mindustry.world.Block;
import nuclearmod.blocks.payload.MissileAssembler;
import nuclearmod.blocks.payload.MissilePartFactory;
import nuclearmod.content.blocks.ModCraftingBlocks;
import nuclearmod.content.blocks.ModDefenseBlocks;
import nuclearmod.content.blocks.ModEnvironmentBlocks;
import nuclearmod.content.blocks.ModPayloadBlocks;

public final class ModBlocks {
    public static Block oreIron;
    public static Block oreUranium;
    public static Block refinariaUranio;
    public static Block siloMisseis;
    public static Block circuitFabric;
    public static Block steelFabric;
    public static Block missilePartA;
    public static Block missilePartB;
    public static Block missilePartC;
    public static MissilePartFactory missileFactory;
    public static MissileAssembler missileAssembler;
    public static Block missileComplete;
    public static Block projetorLinear;

    private ModBlocks() {}

    public static void load() {
        ModContentLoader.loadBlocks();

        oreIron = ModEnvironmentBlocks.oreIron;
        oreUranium = ModEnvironmentBlocks.oreUranium;

        refinariaUranio = ModCraftingBlocks.uraniumRefinery;
        circuitFabric = ModCraftingBlocks.circuitFactory;
        steelFabric = ModCraftingBlocks.steelFactory;

        missilePartA = ModPayloadBlocks.missilePartA;
        missilePartB = ModPayloadBlocks.missilePartB;
        missilePartC = ModPayloadBlocks.missilePartC;
        missileFactory = ModPayloadBlocks.missileFactory;
        missileComplete = ModPayloadBlocks.missileComplete;
        missileAssembler = ModPayloadBlocks.missileAssembler;

        siloMisseis = ModDefenseBlocks.missileSilo;
        projetorLinear = ModDefenseBlocks.linearShieldProjector;
    }
}
