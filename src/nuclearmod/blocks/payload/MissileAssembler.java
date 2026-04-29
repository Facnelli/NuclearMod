package nuclearmod.blocks.payload;

import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadBlock;
import nuclearmod.config.ModBalance;
import nuclearmod.config.ModBalanceDefaults;
import nuclearmod.content.blocks.ModPayloadBlocks;
import nuclearmod.ui.ModUI;

public class MissileAssembler extends PayloadBlock {
    public float craftTime = ModBalanceDefaults.MissileProduction.MISSILE_ASSEMBLER_CRAFT_TIME_FRAMES;
    public Block outputBlock;

    public MissileAssembler(String name) {
        super(name);
        update = true;
        solid = true;
        hasPower = true;
        outputsPayload = true;
        acceptsPayload = true;
    }

    public class MissileAssemblerBuild extends PayloadBlockBuild<Payload> {
        public boolean hasPartA, hasPartB, hasPartC;
        public float progress;

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            if (!(payload instanceof BuildPayload bp)) return false;
            Block b = bp.block();

            if (b == ModPayloadBlocks.missilePartA && !hasPartA) return true;
            if (b == ModPayloadBlocks.missilePartB && !hasPartB) return true;
            if (b == ModPayloadBlocks.missilePartC && !hasPartC) return true;

            return false;
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            Block b = ((BuildPayload) payload).block();
            if (b == ModPayloadBlocks.missilePartA) hasPartA = true;
            else if (b == ModPayloadBlocks.missilePartB) hasPartB = true;
            else if (b == ModPayloadBlocks.missilePartC) hasPartC = true;
        }

        @Override
        public void updateTile() {
            craftTime = ModBalance.MissileProduction.missileAssemblerCraftTimeFrames;
            if (payload != null) {
                moveOutPayload();
                return;
            }

            if (hasPartA && hasPartB && hasPartC && efficiency > 0) {
                progress += edelta() / craftTime;

                if (progress >= 1f) {
                    hasPartA = false;
                    hasPartB = false;
                    hasPartC = false;
                    progress = 0f;
                    payload = new BuildPayload(outputBlock, team);
                }
            }
        }

        @Override
        public void display(arc.scene.ui.layout.Table table) {
            super.display(table);
            ModUI.buildAssemblerTable(table, this); // Chamada limpa para a UI!
        }

        @Override
        public void write(arc.util.io.Writes write) {
            super.write(write);
            write.bool(hasPartA);
            write.bool(hasPartB);
            write.bool(hasPartC);
            write.f(progress);
        }

        @Override
        public void read(arc.util.io.Reads read, byte revision) {
            super.read(read, revision);
            hasPartA = read.bool();
            hasPartB = read.bool();
            hasPartC = read.bool();
            progress = read.f();
        }
    }
}
