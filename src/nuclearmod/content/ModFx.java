package nuclearmod.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.Rand;
import mindustry.entities.Effect;
import mindustry.graphics.Layer; // Importação para desenhar as cinzas no chão

public class ModFx {
    public static Effect nukeExplosion;
    public static Effect nukeScorch; // Novo efeito declarado

    public static void load() {
        nukeExplosion = new Effect(1200f, 2000f, e -> {
            Rand rand = new Rand();
            float progressOnda = Math.min(e.fin() * 2.5f, 1f);
            float raioOndaChoque = 1800f * arc.math.Interp.pow3Out.apply(progressOnda);

            Draw.color(Color.valueOf("d2b48c"), Color.darkGray, progressOnda);
            Lines.stroke(55f * (1f - progressOnda));
            Lines.circle(e.x, e.y, raioOndaChoque);

            float tempoSubida = Math.min(e.fin() * 4f, 1f);
            float alturaTronco = 300f * arc.math.Interp.pow3Out.apply(tempoSubida);
            float alfaTronco = 1f - Math.min(e.fin() * 1.5f, 1f);

            if (alfaTronco > 0) {
                Draw.color(Color.orange, Color.darkGray, e.fin() * 1.5f);
                Draw.alpha(alfaTronco);
                Lines.stroke(60f * alfaTronco);
                Lines.line(e.x, e.y, e.x, e.y + alturaTronco);
            }

            rand.setSeed(e.id);
            if (e.fin() < 0.05f) {
                Draw.color(Color.white, Color.orange, e.fin() * 20f);
                Fill.circle(e.x, e.y + alturaTronco, 200f * (1f - e.fin() * 20f));
            }

            for(int i = 0; i < 80; i++){
                float angulo = rand.random(360f);
                float distanciaBase = rand.random(150f);
                float tamanhoParticula = rand.random(30f, 60f);
                float expansao = distanciaBase * (1f + (e.fin() * 0.8f));
                float px = e.x + (Mathf.cosDeg(angulo) * expansao);
                float py = e.y + alturaTronco + (Mathf.sinDeg(angulo) * expansao);
                py += e.fin() * 100f;

                Draw.color(Color.orange, Color.darkGray, Math.min(e.fin() * 3f, 1f));
                if(e.fin() > 0.6f) Draw.color(Color.darkGray, Color.lightGray, (e.fin() - 0.6f) * 2.5f);
                Draw.alpha(e.fout(arc.math.Interp.pow2Out));
                Fill.circle(px, py, tamanhoParticula);
            }
            Draw.reset();
        });

        // Efeito de Cinzas/Terra Arrasada (2 minutos)
        nukeScorch = new Effect(7200f, 3000f, e -> {
            Draw.z(Layer.scorch);
            Draw.color(Color.black);
            Draw.alpha(0.8f * (1f - e.fin()));
            Fill.square(e.x, e.y, 8f);
            Draw.reset();
        });
    }
}