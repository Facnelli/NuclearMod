package nuclearmod.ui;

import arc.func.Floatc;
import arc.func.Floatp;
import arc.func.Func;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import nuclearmod.config.ModBalance;
import nuclearmod.config.ModBalanceDefaults;

public class ModBalanceDialog extends BaseDialog {
    public ModBalanceDialog() {
        super("Balanceamento do Mod");
        addCloseButton();

        buttons.button("Restaurar defaults", Icon.refresh, () -> {
            ModBalance.resetToDefaults();
            rebuild();
        }).size(240f, 64f);

        shown(this::rebuild);
    }

    private void rebuild() {
        cont.clear();
        cont.pane(table -> {
            table.defaults().growX().pad(6f);

            addSection(table, "Fogo Azul");
            addSlider(table, "Vida do fogo azul (seg)", 1f, 600f, 1f,
                () -> ModBalance.BlueFire.bulletLifetimeFrames / ModBalanceDefaults.FRAMES_PER_SECOND,
                value -> ModBalance.BlueFire.bulletLifetimeFrames = value * ModBalanceDefaults.FRAMES_PER_SECOND,
                value -> Strings.fixed(value, 0) + " s");
            addSlider(table, "Dano estrutural (%)", 0f, 25f, 0.5f,
                () -> ModBalance.BlueFire.buildingDamagePercentPerTrigger * 100f,
                value -> ModBalance.BlueFire.buildingDamagePercentPerTrigger = value / 100f,
                value -> Strings.fixed(value, 1) + "%");
            addSlider(table, "Chance de dano por segundo", 0f, 6f, 0.1f,
                () -> ModBalance.BlueFire.buildingDamageChancePerFrame * ModBalanceDefaults.FRAMES_PER_SECOND,
                value -> ModBalance.BlueFire.buildingDamageChancePerFrame = value / ModBalanceDefaults.FRAMES_PER_SECOND,
                value -> Strings.fixed(value, 1) + "/s");
            addSlider(table, "Tentativas de dispersao por segundo", 0f, 10f, 0.1f,
                () -> ModBalance.BlueFire.spreadAttemptChancePerFrame * ModBalanceDefaults.FRAMES_PER_SECOND,
                value -> ModBalance.BlueFire.spreadAttemptChancePerFrame = value / ModBalanceDefaults.FRAMES_PER_SECOND,
                value -> Strings.fixed(value, 1) + "/s");
            addSlider(table, "Chance por vizinho (%)", 0f, 100f, 1f,
                () -> ModBalance.BlueFire.spreadNeighborChance * 100f,
                value -> ModBalance.BlueFire.spreadNeighborChance = value / 100f,
                value -> Strings.fixed(value, 0) + "%");
            addSlider(table, "Raio de dispersao (tiles)", 1f, 8f, 1f,
                () -> ModBalance.BlueFire.spreadRadiusTiles,
                value -> ModBalance.BlueFire.spreadRadiusTiles = Math.round(value),
                value -> Strings.fixed(value, 0) + " tiles");
            addSlider(table, "Raio em unidades (pixels)", 4f, 64f, 1f,
                () -> ModBalance.BlueFire.unitAffectRadius,
                value -> ModBalance.BlueFire.unitAffectRadius = value,
                value -> Strings.fixed(value, 0) + " px");
            addSlider(table, "Duracao do status (seg)", 1f, 600f, 1f,
                () -> ModBalance.BlueFire.unitStatusDurationFrames / ModBalanceDefaults.FRAMES_PER_SECOND,
                value -> ModBalance.BlueFire.unitStatusDurationFrames = value * ModBalanceDefaults.FRAMES_PER_SECOND,
                value -> Strings.fixed(value, 0) + " s");

            addSection(table, "Nuclear");
            addSlider(table, "Raio da explosao (tiles)", 5f, 100f, 1f,
                () -> ModBalance.Nuclear.blastRadiusTiles,
                value -> ModBalance.Nuclear.blastRadiusTiles = Math.round(value),
                value -> Strings.fixed(value, 0) + " tiles");
            addSlider(table, "Raio de morte instantanea (tiles)", 1f, 80f, 1f,
                () -> ModBalance.Nuclear.unitInstantKillRadiusTiles,
                value -> ModBalance.Nuclear.unitInstantKillRadiusTiles = value,
                value -> Strings.fixed(value, 0) + " tiles");
            addSlider(table, "Dano severo em estruturas", 0f, 5000f, 25f,
                () -> ModBalance.Nuclear.severeBuildingDamage,
                value -> ModBalance.Nuclear.severeBuildingDamage = value,
                value -> Strings.fixed(value, 0));
            addSlider(table, "Dano externo em unidades", 0f, 10000f, 50f,
                () -> ModBalance.Nuclear.unitOuterDamage,
                value -> ModBalance.Nuclear.unitOuterDamage = value,
                value -> Strings.fixed(value, 0));
            addSlider(table, "Faíscas nucleares", 0f, 200f, 1f,
                () -> ModBalance.Nuclear.sparkCount,
                value -> ModBalance.Nuclear.sparkCount = Math.round(value),
                value -> Strings.fixed(value, 0));

            addSection(table, "Escudo");
            addSlider(table, "Alcance do link", 20f, 400f, 4f,
                () -> ModBalance.Shield.range,
                value -> ModBalance.Shield.range = value,
                value -> Strings.fixed(value, 0) + " px");
            addSlider(table, "Vida do escudo", 100f, 20000f, 100f,
                () -> ModBalance.Shield.health,
                value -> ModBalance.Shield.health = value,
                value -> Strings.fixed(value, 0));
        }).grow().pad(12f);
    }

    private void addSection(Table table, String title) {
        table.row();
        table.add("[accent]" + title).left().padTop(14f).padBottom(4f);
        table.row();
    }

    private void addSlider(Table table, String title, float min, float max, float step,
                           Floatp getter, Floatc setter, Func<Float, String> formatter) {
        Label valueLabel = new Label(formatter.get(getter.get()));
        Slider slider = new Slider(min, max, step, false);
        slider.setValue(getter.get());
        slider.changed(() -> {
            setter.get(slider.getValue());
            valueLabel.setText(formatter.get(getter.get()));
        });

        table.table(row -> {
            row.left();
            row.add(title).left().growX();
            row.add(valueLabel).right().padLeft(8f);
        }).growX();
        table.row();
        table.add(slider).growX();
        table.row();
    }
}
