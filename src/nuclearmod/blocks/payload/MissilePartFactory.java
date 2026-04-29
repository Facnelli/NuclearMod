package nuclearmod.blocks.payload;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.blocks.payloads.PayloadBlock;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.Block;
import mindustry.ui.Styles;
import nuclearmod.config.ModBalance;
import nuclearmod.config.ModBalanceDefaults;
import nuclearmod.ui.ModUI;

public class MissilePartFactory extends PayloadBlock {
    public Seq<PartRecipe> plans = new Seq<>();
    public float craftTime = ModBalanceDefaults.MissileProduction.MISSILE_PART_FACTORY_CRAFT_TIME_FRAMES;

    public MissilePartFactory(String name) {
        super(name);
        update = true;
        solid = true;
        hasPower = true;
        hasItems = true;
        configurable = true;
        outputsPayload = true;

        config(Integer.class, (MissilePartFactoryBuild tile, Integer i) -> {
            if(!configurable) return;
            if(tile.plan == i) return;
            tile.plan = i;
            tile.progress = 0;
        });
    }

    public static class PartRecipe {
        public Block output;
        public ItemStack[] requirements;

        public PartRecipe(Block output, ItemStack[] requirements) {
            this.output = output;
            this.requirements = requirements;
        }
    }

    public class MissilePartFactoryBuild extends PayloadBlockBuild<BuildPayload> {
        public int plan = -1;
        public float progress;

        @Override
        public void buildConfiguration(Table table) {
            table.background(Styles.black6);
            for (int i = 0; i < plans.size; i++) {
                int index = i;
                PartRecipe recipe = plans.get(i);
                table.button(b -> b.image(recipe.output.uiIcon).size(40f), Styles.clearNoneTogglei, () -> {
                    configure(index);
                }).size(60f).checked(plan == index).pad(5f);
            }
        }

        @Override
        public void updateTile() {
            craftTime = ModBalance.MissileProduction.missilePartFactoryCraftTimeFrames;
            if (plan < 0 || plan >= plans.size) return;

            PartRecipe current = plans.get(plan);

            if (payload != null) {
                moveOutPayload();
                return;
            }

            if (efficiency > 0 && items.has(current.requirements)) {
                progress += edelta() / craftTime;

                if (progress >= 1f) {
                    consumeItems(current.requirements);
                    payload = new BuildPayload(current.output, team);
                    progress = 0f;
                }
            }
        }

        @Override
        public void display(arc.scene.ui.layout.Table table) {
            super.display(table);
            if (plan >= 0 && plan < plans.size) {
                ModUI.buildFactoryTable(table, this, plans.get(plan)); // Chamada limpa para a UI!
            }
        }

        private void consumeItems(ItemStack[] requirements) {
            for(ItemStack stack : requirements){
                items.remove(stack.item, stack.amount);
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            if (plan < 0 || plan >= plans.size) return false;
            PartRecipe current = plans.get(plan);

            for(ItemStack req : current.requirements) {
                if(req.item == item && items.get(item) < getMaximumAccepted(item)) return true;
            }
            return false;
        }

        @Override
        public void write(arc.util.io.Writes write){
            super.write(write);
            write.i(plan);
            write.f(progress);
        }

        @Override
        public void read(arc.util.io.Reads read, byte revision){
            super.read(read, revision);
            plan = read.i();
            progress = read.f();
        }
    }
}
