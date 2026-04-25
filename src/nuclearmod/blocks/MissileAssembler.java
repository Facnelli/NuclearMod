package nuclearmod.blocks;

import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadBlock;
import nuclearmod.content.ModBlocks;

public class MissileAssembler extends PayloadBlock {
    public float craftTime = 9000f; // 150 segundos para montar o míssil inteiro
    public Block outputBlock;

    public MissileAssembler(String name) {
        super(name);
        update = true;
        solid = true;
        hasPower = true;
        outputsPayload = true; // Permite cuspir carga
        acceptsPayload = true; // Permite engolir carga
    }

    public class MissileAssemblerBuild extends PayloadBlockBuild<Payload> {
        public boolean hasPartA, hasPartB, hasPartC;
        public float progress;

        // Verifica se a carga entrando é uma das peças e se já não temos ela guardada
        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            if (!(payload instanceof BuildPayload bp)) return false;
            Block b = bp.block();

            if (b == ModBlocks.missilePartA && !hasPartA) return true;
            if (b == ModBlocks.missilePartB && !hasPartB) return true;
            if (b == ModBlocks.missilePartC && !hasPartC) return true;

            return false;
        }

        // Quando a peça entra, marcamos ela como "recebida" e destruímos o payload de entrada
        @Override
        public void handlePayload(Building source, Payload payload) {
            Block b = ((BuildPayload) payload).block();
            if (b == ModBlocks.missilePartA) hasPartA = true;
            else if (b == ModBlocks.missilePartB) hasPartB = true;
            else if (b == ModBlocks.missilePartC) hasPartC = true;
        }

        @Override
        public void updateTile() {
            // Se já tem um míssil pronto na saída, tenta empurrar pra esteira
            if (payload != null) {
                moveOutPayload();
                return;
            }

            // Se tiver as 3 peças e energia, começa a montar
            if (hasPartA && hasPartB && hasPartC && efficiency > 0) {
                progress += edelta() / craftTime;

                if (progress >= 1f) {
                    // Consome as peças
                    hasPartA = false;
                    hasPartB = false;
                    hasPartC = false;
                    progress = 0f;

                    // Cria o Míssil Completo e joga na saída
                    payload = new BuildPayload(outputBlock, team);
                }
            }
        }

        @Override
        public void display(arc.scene.ui.layout.Table table) {
            super.display(table);

            table.row();
            // Barra de progresso para a montagem final
            table.add(new mindustry.ui.Bar("Progresso de Montagem", arc.graphics.Color.acid, () -> progress))
                    .pad(10f).width(200f).height(20f);

            table.row();
            table.table(t -> {
                t.add("[lightgray]Peças no Inventário:").left().padBottom(4f).row();
                t.table(icons -> {
                    icons.image(nuclearmod.content.ModBlocks.missilePartA.uiIcon).size(32f).color(hasPartA ? arc.graphics.Color.white : arc.graphics.Color.darkGray).padRight(5f);
                    icons.image(nuclearmod.content.ModBlocks.missilePartB.uiIcon).size(32f).color(hasPartB ? arc.graphics.Color.white : arc.graphics.Color.darkGray).padRight(5f);
                    icons.image(nuclearmod.content.ModBlocks.missilePartC.uiIcon).size(32f).color(hasPartC ? arc.graphics.Color.white : arc.graphics.Color.darkGray);
                }).left();
            }).padTop(8f).left();
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