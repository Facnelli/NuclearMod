package nuclearmod.ui;

import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.type.ItemStack;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import nuclearmod.blocks.payload.MissileAssembler;
import nuclearmod.blocks.payload.MissilePartFactory;
import nuclearmod.content.blocks.ModPayloadBlocks;


public class ModUI {
    private static boolean loaded;
    private static ModBalanceDialog balanceDialog;

    public static void load() {
        if (loaded) return;
        loaded = true;

        balanceDialog = new ModBalanceDialog();
        Vars.ui.settings.addCategory("Nuclear Mod", Icon.settings, table -> {
            table.button("Abrir balanceamento", Icon.edit, Styles.defaultt, balanceDialog::show)
                .growX()
                .height(64f)
                .pad(6f);
            table.row();
        });
    }

    // Cria a interface da Montadora de Mísseis
    public static void buildAssemblerTable(Table table, MissileAssembler.MissileAssemblerBuild build) {
        table.row();
        // Barra de progresso para a montagem final
        table.add(new Bar("Progresso de Montagem", Color.acid, () -> build.progress))
                .pad(10f).width(200f).height(20f);

        table.row();
        table.table(t -> {
            t.add("[lightgray]Peças no Inventário:").left().padBottom(4f).row();
            t.table(icons -> {
                icons.image(ModPayloadBlocks.missilePartA.uiIcon).size(32f)
                        .update(i -> {
                            i.setColor(build.hasPartA ? Color.white : Color.darkGray);
                        }).padRight(5f);

                icons.image(ModPayloadBlocks.missilePartB.uiIcon).size(32f)
                        .update(i -> {
                            i.setColor(build.hasPartB ? Color.white : Color.darkGray);
                        }).padRight(5f);

                icons.image(ModPayloadBlocks.missilePartC.uiIcon).size(32f)
                        .update(i -> {
                            i.setColor(build.hasPartC ? Color.white : Color.darkGray);
                        });
            }).left();
        }).padTop(8f).left();
    }

    // Cria a interface da Fábrica de Peças
    public static void buildFactoryTable(Table table, MissilePartFactory.MissilePartFactoryBuild build, MissilePartFactory.PartRecipe current) {
        table.row();
        // Adiciona a barra de progresso
        table.add(new Bar("Progresso de Produção", Color.gold, () -> build.progress))
                .pad(10f).width(200f).height(20f);

        table.row();
        table.table(t -> {
            t.add("[lightgray]Requisitos:").left().padBottom(4f).row();
            for (ItemStack stack : current.requirements) {
                t.image(stack.item.uiIcon).size(24f).padRight(4f);
                t.label(() -> {
                    boolean temSuficiente = build.items.get(stack.item) >= stack.amount;
                    return (temSuficiente ? "[white]" : "[scarlet]") + build.items.get(stack.item) + " / " + stack.amount;
                }).left().padRight(10f).row();
            }
        }).left();
    }
}
