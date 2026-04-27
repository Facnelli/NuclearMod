package nuclearmod.content;

import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
//import mindustry.content.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.DrawTurret;
import nuclearmod.blocks.LinearShieldProjector;
import nuclearmod.blocks.MissileAssembler;
import nuclearmod.blocks.SiloNuclear;
import nuclearmod.blocks.MissilePartFactory;

import static mindustry.type.ItemStack.with;

public class ModBlocks {
    public static Block oreIron, oreUranium, refinariaUranio, siloMisseis, circuitFabric, steelFabric;
    public static Block missilePartA, missilePartB, missilePartC;
    public static MissilePartFactory missileFactory;
    public static MissileAssembler missileAssembler;
    public static Block missileComplete;
    public static Block projetorLinear;

    public static void load() {
        // --- MINÉRIOS ---
        oreIron = new OreBlock("ore-iron", ModItems.iron) {{
            localizedName = "Minério de ferro";
            oreDefault = true;
            oreScale = 30.0f;
            oreThreshold = 0.4f;
            variants = 1;
        }};

        oreUranium = new OreBlock("ore-uranium", ModItems.uranio) {{
            localizedName = "Minério de Urânio";
            oreDefault = true;
            oreScale = 24.0f;
            oreThreshold = 0.88f;
            variants = 1;
        }};

        // --- REFINARIA DE URÂNIO ---
        refinariaUranio = new GenericCrafter("refinaria-uranio") {{
            localizedName = "Refinaria de Urânio";
            description = "Utiliza escória fervente e plastânio para purificar o urânio bruto em urânio enriquecido.";
            size = 3;
            health = 360;
            category = Category.crafting;
            alwaysUnlocked = true;

            requirements(Category.crafting, with(Items.copper, 200, Items.lead, 150, Items.silicon, 100, Items.plastanium, 50));

            craftTime = 120f;
            itemCapacity = 20;
            liquidCapacity = 50;
            outputItem = new ItemStack(ModItems.uranioEnriquecido, 1);

            consumePower(4.0f);
            consumeItems(with(ModItems.uranio, 2, Items.plastanium, 1));
            consumeLiquid(Liquids.slag, 0.2f);

            updateEffect = Fx.smeltsmoke;
            craftEffect = Fx.formsmoke;
            //ambientSound = Sounds.smelter;
            ambientSoundVolume = 0.5f;
            destroyEffect = Fx.reactorExplosion;
            //destroySound = Sounds.explosionbig;
        }};

        // --- SILO NUCLEAR ---
        siloMisseis = new SiloNuclear("silo-misseis") {{
            localizedName = "Silo de misseis";
            description = "Silo de mísseis tático de controle manual. Assuma o controle para disparar e não atire perto demais.";
            size = 5;
            health = 1200;
            clipSize = 6000;
            category = Category.turret;
            alwaysUnlocked = true;
            rotateSpeed = 999f;
            shootCone = 1f;

            drawer = new DrawTurret() {{
                basePrefix = ""; // Usa a sprite padrão
            }};

            requirements(Category.turret, with(Items.lead, 2000, Items.silicon, 1200, Items.plastanium, 500, Items.surgeAlloy, 500));

            range = 6000f;
            reload = 300f;
            inaccuracy = 0f;
            itemCapacity = 10;
            ammoPerShot = 10;
            maxAmmo = 10;

            targetAir = false;
            targetGround = false;
            playerControllable = true;
            destroyEffect = Fx.reactorExplosion;

            // Definimos a munição aqui, sem precisar sobrescrever o init() no SiloNuclear!
            ammo(
                    ModItems.uranioEnriquecido, new SiloNuclear.NukeBulletType()
            );
        }};
        // --- Fábrica de Circuitos eletronicos ---
        circuitFabric = new GenericCrafter("circuit-fabric") {{
            localizedName = "Fabrica de Circuitos";
            description = "Cria circuitos com Silicio, Aço e Liga de Surto";
            size = 3;
            health = 400;
            category = Category.crafting;
            alwaysUnlocked = true;

            requirements(Category.crafting, with(Items.copper, 200, Items.lead, 150, Items.silicon, 100, Items.surgeAlloy, 50));

            craftTime = 1000f;
            itemCapacity = 500;
            outputItem = new ItemStack(ModItems.circuit, 1);

            consumePower(5.0f);
            consumeItems(with(Items.silicon, 200, ModItems.steel, 100, Items.surgeAlloy, 300));

            updateEffect = Fx.smeltsmoke;
            craftEffect = Fx.formsmoke;
            //ambientSound = Sounds.smelter;
            ambientSoundVolume = 0.5f;
        }};

        // --- Fábrica de aço ---
        steelFabric = new GenericCrafter("steel-fabric") {{
            localizedName = "Fabrica de aço";
            description = "Cria Aço com Ferro, Grafite e escória";
            size = 3;
            health = 400;
            category = Category.crafting;
            alwaysUnlocked = true;

            requirements(Category.crafting, with(Items.titanium, 100, Items.lead, 50, Items.silicon, 100));

            craftTime = 120f;
            itemCapacity = 250;
            outputItem = new ItemStack(ModItems.steel, 1);

            consumePower(2.0f);
            consumeItems(with(Items.graphite, 4, ModItems.iron, 5.5));
            consumeLiquid(Liquids.slag, 0.3f);

            updateEffect = Fx.smeltsmoke;
            craftEffect = Fx.formsmoke;
            //ambientSound = Sounds.smelter;
            ambientSoundVolume = 0.5f;
        }};

        // --- PARTES DO MÍSSIL E FÁBRICA  ---
        missilePartA = new Block("missile-part-a") {{ size = 3; solid = true; destructible = true; }};
        missilePartB = new Block("missile-part-b") {{ size = 3; solid = true; destructible = true; }};
        missilePartC = new Block("missile-part-c") {{ size = 3; solid = true; destructible = true; }};

        missileFactory = new MissilePartFactory("missile-factory") {{
            localizedName = "Fábrica de Peças de Míssil";
            requirements(Category.crafting, with(Items.titanium, 1500, Items.silicon, 150));
            size = 5;
            itemCapacity = 150;

            rotate = true;

            plans.add(
                    new MissilePartFactory.PartRecipe(missilePartA, with(ModItems.steel, 30, Items.silicon, 20, Items.plastanium, 10)),
                    new MissilePartFactory.PartRecipe(missilePartB, with(ModItems.steel, 25, ModItems.circuit, 15, ModItems.uranioEnriquecido, 40)), // Usando Urânio Enriquecido
                    new MissilePartFactory.PartRecipe(missilePartC, with(Items.titanium, 30, ModItems.steel, 20, Items.plastanium, 15))
            );
        }};

        // --- MÍSSIL COMPLETO ---
        missileComplete = new Block("missile-complete") {{
            size = 3; // Ele será gigante na esteira! (96x96 pixels de imagem)
            solid = true;
            destructible = true;
            health = 2000;
        }};

        // --- MONTADORA DE MÍSSEIS ---
        missileAssembler = new MissileAssembler("missile-assembler") {{
            localizedName = "Montadora de Mísseis";
            description = "Recebe as 3 partes do míssil através de esteiras de carga e finaliza a montagem.";
            size = 5;
            outputBlock = missileComplete;

            rotate = true;

            requirements(Category.crafting, with(ModItems.steel, 2000, Items.surgeAlloy, 500, Items.silicon, 800));
            consumeLiquid(Liquids.oil,0.5f);
            consumePower(30f); // Consome bastante energia para soldar as peças
        }};

        projetorLinear = new LinearShieldProjector("projetor-linear") {{
            localizedName = "Projetor de Escudo Linear";
            description = "Um potente projetor que se conecta a outro nó para formar uma barreira de força capaz de suportar radiação e ondas de choque nucleares.";
            size = 2;
            health = 1000;
            category = Category.effect; // Fica na aba de escudos/efeitos


            requirements(Category.effect, with(Items.titanium, 100, Items.silicon, 50, Items.surgeAlloy, 25, ModItems.steel, 10));

            consumePower(5f); // O custo energético para manter a barreira ligada
        }};

    }
}