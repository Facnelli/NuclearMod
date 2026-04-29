package nuclearmod;

import arc.Events;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.mod.Mod;
import nuclearmod.content.ModContentLoader;
import nuclearmod.ui.ModUI;

public class NuclearMod extends Mod {

    public NuclearMod() {
        Log.info("Nuclear Mod inicializado!");
        Events.on(ClientLoadEvent.class, event -> {
            if (!Vars.headless) {
                ModUI.load();
            }
        });
    }

    @Override
    public void loadContent() {
        Log.info("Carregando conteúdo do Nuclear Mod...");
        ModContentLoader.load();
        Log.info("Conteúdo carregado e pronto para lançamento!");
    }
}
