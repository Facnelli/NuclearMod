package nuclearmod;

import arc.util.Log;
import mindustry.mod.Mod;
import nuclearmod.content.ModBlocks;
import nuclearmod.content.ModFx;
import nuclearmod.content.ModItems;

public class NuclearMod extends Mod {

    public NuclearMod() {
        Log.info("Nuclear Mod inicializado!");
    }

    @Override
    public void loadContent() {
        Log.info("Carregando conteúdo do Nuclear Mod...");

        ModItems.load();
        ModFx.load();
        ModBlocks.load();

        Log.info("Conteúdo carregado e pronto para lançamento!");
    }
}