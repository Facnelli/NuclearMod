package nuclearmod.content.blocks;

import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import nuclearmod.content.ModItems;

import static mindustry.type.ItemStack.with;

public final class ModCraftingBlocks {
    public static Block uraniumRefinery;
    public static Block circuitFactory;
    public static Block steelFactory;

    private ModCraftingBlocks() {}

    public static void load() {
        uraniumRefinery = new GenericCrafter("refinaria-uranio") {{
            localizedName = "Refinaria de Uranio";
            description = "Utiliza escoria fervente e plastanio para purificar o uranio bruto em uranio enriquecido.";
            size = 3;
            health = 360;
            category = Category.crafting;
            alwaysUnlocked = true;

            requirements(Category.crafting, with(Items.copper, 200, Items.lead, 150, Items.silicon, 100, Items.plastanium, 50));

            craftTime = 60f;
            itemCapacity = 5;
            liquidCapacity = 50;
            outputItem = new ItemStack(ModItems.uranioEnriquecido, 1);

            consumePower(4.0f);
            consumeItems(with(ModItems.uranio, 2, Items.plastanium, 1));
            consumeLiquid(Liquids.slag, 0.2f);

            updateEffect = Fx.smeltsmoke;
            craftEffect = Fx.formsmoke;
            ambientSoundVolume = 0.5f;
            destroyEffect = Fx.reactorExplosion;
        }};

        circuitFactory = new GenericCrafter("circuit-fabric") {{
            localizedName = "Fabrica de Circuitos";
            description = "Cria circuitos com silicio, aco e liga de surto.";
            size = 3;
            health = 400;
            category = Category.crafting;
            alwaysUnlocked = true;

            requirements(Category.crafting, with(Items.copper, 200, Items.lead, 150, Items.silicon, 100, Items.surgeAlloy, 50));

            craftTime = 300f;
            itemCapacity = 10;
            outputItem = new ItemStack(ModItems.circuit, 1);

            consumePower(5.0f);
            consumeItems(with(Items.silicon, 10, ModItems.steel, 10, Items.surgeAlloy, 10));

            updateEffect = Fx.smeltsmoke;
            craftEffect = Fx.formsmoke;
            ambientSoundVolume = 0.5f;
        }};

        steelFactory = new GenericCrafter("steel-fabric") {{
            localizedName = "Fabrica de aco";
            description = "Cria aco com ferro, grafite e escoria.";
            size = 3;
            health = 400;
            category = Category.crafting;
            alwaysUnlocked = true;

            requirements(Category.crafting, with(Items.titanium, 100, Items.lead, 50, Items.silicon, 100));

            craftTime = 120f;
            itemCapacity = 20;
            outputItem = new ItemStack(ModItems.steel, 2);

            consumePower(2.0f);
            consumeItems(with(Items.graphite, 4, ModItems.iron, 5));
            consumeLiquid(Liquids.slag, 0.2f);

            updateEffect = Fx.smeltsmoke;
            craftEffect = Fx.formsmoke;
            ambientSoundVolume = 0.5f;
        }};
    }
}
