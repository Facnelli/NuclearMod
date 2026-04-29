package nuclearmod.content.blocks;

import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.world.Block;
import nuclearmod.blocks.payload.MissileAssembler;
import nuclearmod.blocks.payload.MissilePartFactory;
import nuclearmod.content.ModFx;
import nuclearmod.content.ModItems;

import static mindustry.type.ItemStack.with;

public final class ModPayloadBlocks {
    public static Block missilePartA;
    public static Block missilePartB;
    public static Block missilePartC;
    public static MissilePartFactory missileFactory;
    public static Block missileComplete;
    public static MissileAssembler missileAssembler;

    private ModPayloadBlocks() {}

    public static void load() {
        missilePartA = new Block("missile-part-a") {{ size = 3; solid = true; destructible = true; }};
        missilePartB = new Block("missile-part-b") {{ size = 3; solid = true; destructible = true; }};
        missilePartC = new Block("missile-part-c") {{ size = 3; solid = true; destructible = true; }};

        missileFactory = new MissilePartFactory("missile-factory") {{
            localizedName = "Fabrica de Pecas de Missil";
            requirements(Category.crafting, with(Items.titanium, 1500, Items.silicon, 150, Items.surgeAlloy, 150));
            size = 5;
            itemCapacity = 1000;
            rotate = true;

            plans.add(
                new MissilePartFactory.PartRecipe(missilePartA, with(ModItems.steel, 30, Items.silicon, 20, Items.plastanium, 100)),
                new MissilePartFactory.PartRecipe(missilePartB, with(ModItems.steel, 25, ModItems.circuit, 15, ModItems.uranioEnriquecido, 400)),
                new MissilePartFactory.PartRecipe(missilePartC, with(Items.titanium, 30, ModItems.steel, 20, Items.plastanium, 150))
            );
        }};

        missileComplete = new Block("missile-complete") {{
            size = 3;
            solid = true;
            destructible = true;
            health = 2000;
            destroyEffect = ModFx.nukeExplosion;
        }};

        missileAssembler = new MissileAssembler("missile-assembler") {{
            localizedName = "Montadora de Misseis";
            description = "Recebe as 3 partes do missil atraves de esteiras de carga e finaliza a montagem.";
            size = 5;
            outputBlock = missileComplete;
            rotate = true;

            requirements(Category.crafting, with(ModItems.steel, 500, Items.surgeAlloy, 500, Items.silicon, 800));
            consumeLiquid(Liquids.oil, 0.1f);
            consumePower(30f);
        }};
    }
}
