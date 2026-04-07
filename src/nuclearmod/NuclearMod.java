package nuclearmod;

import arc.Events;
import mindustry.game.EventType.TapEvent;
import mindustry.gen.Call;
import mindustry.mod.Mod;

import mindustry.world.blocks.defense.turrets.ItemTurret.ItemTurretBuild;

public class NuclearMod extends Mod {

    @Override
    public void init() {
        Events.on(TapEvent.class, event -> {

            // 1. Verificamos se o que foi clicado é uma construção válida
            if (event.tile != null && event.tile.build != null) {

                // 2. Verificamos se essa construção é especificamente uma ItemTurretBuild
                if (event.tile.build instanceof ItemTurretBuild) {

                    // Criamos uma variável 'build' que nos permite ver o que tem dentro da torre
                    ItemTurretBuild build = (ItemTurretBuild) event.tile.build;

                    // 3. Checamos se o nome do bloco está correto E se ela tem munição
                    if (build.block.name.equals("nuclear-mod-silo-misseis") && build.hasAmmo()) {

                        // O alarme só toca se houver balas/mísseis carregados!
                        Call.announce("[scarlet]⚠ ALERTA NUCLEAR: [white]Silo carregado e ativado por " + event.player.name + "!");
                    }
                }
            }
        });
    }
}