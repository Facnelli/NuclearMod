package nuclearmod.content.blocks;

import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.draw.DrawTurret;
import nuclearmod.blocks.defense.LinearShieldProjector;
import nuclearmod.blocks.defense.SiloNuclear;
import nuclearmod.content.ModItems;
import nuclearmod.type.bullet.NukeBulletType;

import static mindustry.type.ItemStack.with;

public final class ModDefenseBlocks {
    public static Block missileSilo;
    public static Block linearShieldProjector;

    private ModDefenseBlocks() {}

    public static void load() {
        missileSilo = new SiloNuclear("silo-misseis") {{
            localizedName = "Silo de misseis";
            description = "Silo de misseis tatico de controle manual. Assuma o controle para disparar e nao atire perto demais.";
            size = 5;
            health = 1200;
            clipSize = 6000;
            category = Category.turret;
            alwaysUnlocked = true;
            rotateSpeed = 999f;
            shootCone = 1f;

            drawer = new DrawTurret() {{
                basePrefix = "";
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

            ammo(ModItems.uranioEnriquecido, new NukeBulletType());
        }};

        linearShieldProjector = new LinearShieldProjector("projetor-linear") {{
            localizedName = "Projetor de Escudo Linear";
            description = "Conecta-se a outros nos para formar uma barreira capaz de suportar radiacao e ondas de choque nucleares.";
            size = 2;
            health = 1000;
            category = Category.effect;

            requirements(Category.effect, with(Items.titanium, 100, Items.silicon, 50, Items.surgeAlloy, 25, ModItems.steel, 10));
            consumePower(5f);
        }};
    }
}
