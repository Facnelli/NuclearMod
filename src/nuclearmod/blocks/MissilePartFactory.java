package nuclearmod.blocks;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.blocks.payloads.PayloadBlock;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.Block;
import mindustry.ui.Styles;

public class MissilePartFactory extends PayloadBlock {
    public Seq<PartRecipe> plans = new Seq<>();
    public float craftTime = 600f;

    public MissilePartFactory(String name) {
        super(name);
        update = true;
        solid = true;
        hasPower = true;
        hasItems = true;
        configurable = true;
        outputsPayload = true;

        // Lógica de Sincronização: garante que apenas UM plano esteja ativo
        config(Integer.class, (MissilePartFactoryBuild tile, Integer i) -> {
            if(!configurable) return;

            // Se o jogador clicar na peça que já está selecionada, não faz nada
            if(tile.plan == i) return;

            // Se for uma peça nova, muda o plano e zera o progresso da fabricação anterior
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
        public int plan = -1; // -1 = nenhuma peça selecionada
        public float progress;

        @Override
        public void buildConfiguration(Table table) {
            // Cria uma tabela de botões. O estilo 'Styles.clearNoneTogglei' faz o botão ficar "aceso" quando selecionado.
            table.background(Styles.black6); // Fundo escuro para a UI ficar bonita

            for (int i = 0; i < plans.size; i++) {
                int index = i;
                PartRecipe recipe = plans.get(i);

                // O método .checked() é o segredo: ele brilha apenas se for o plano atual
                table.button(b -> b.image(recipe.output.uiIcon).size(40f), Styles.clearNoneTogglei, () -> {
                    configure(index); // Envia a configuração para o servidor/jogo
                }).size(60f).checked(plan == index).pad(5f);
            }
        }

        @Override
        public void updateTile() {
            // Se não houver peça selecionada, a fábrica fica parada
            if (plan < 0 || plan >= plans.size) return;

            PartRecipe current = plans.get(plan);

            // Se já tem uma peça pronta na saída, espera ela ser levada
            if (payload != null) {
                moveOutPayload();
                return;
            }

            // Lógica de produção (Consumo de energia + Itens)
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
                PartRecipe current = plans.get(plan);

                table.row();
                // Adiciona a barra de progresso (Nome, Cor, Valor de 0 a 1)
                table.add(new mindustry.ui.Bar("Progresso de Produção", arc.graphics.Color.gold, () -> progress))
                        .pad(10f).width(200f).height(20f);

                table.row();
                table.table(t -> {
                    t.add("[lightgray]Requisitos:").left().padBottom(4f).row();
                    for (ItemStack stack : current.requirements) {
                        t.image(stack.item.uiIcon).size(24f).padRight(4f);
                        t.label(() -> {
                            boolean temSuficiente = items.get(stack.item) >= stack.amount;
                            return (temSuficiente ? "[white]" : "[scarlet]") + items.get(stack.item) + " / " + stack.amount;
                        }).left().padRight(10f).row();
                    }
                }).left();
            }
        }

        // Função auxiliar para consumir itens de uma vez
        private void consumeItems(ItemStack[] requirements) {
            for(ItemStack stack : requirements){
                items.remove(stack.item, stack.amount);
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            // Só aceita itens se houver uma peça selecionada e se o item fizer parte da receita dela
            if (plan < 0 || plan >= plans.size) return false;
            PartRecipe current = plans.get(plan);

            for(ItemStack req : current.requirements) {
                if(req.item == item && items.get(item) < getMaximumAccepted(item)) return true;
            }
            return false;
        }

        // Salva a peça selecionada quando você fecha o jogo/mapa
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