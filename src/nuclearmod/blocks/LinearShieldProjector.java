package nuclearmod.blocks;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.game.EventType.ResetEvent;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;

public class LinearShieldProjector extends Block {
    public static Seq<LinearShieldBuild> activeShields = new Seq<>();

    // Previne vazamento de memória limpando a rede ao trocar de mapa
    static {
        Events.on(ResetEvent.class, event -> activeShields.clear());
    }

    public float shieldRange = 160f;
    public int maxLinks = 3;
    public float shieldHealth = 2500f;
    public float cooldownNormal = 1.5f;
    public float cooldownBrokenBase = 0.5f;

    public LinearShieldProjector(String name) {
        super(name);
        update = true;
        solid = true;
        hasPower = true;
        configurable = true;
        group = BlockGroup.projectors;

        // Evita que o laser desapareça quando o centro da tela se afasta do poste
        clipSize = 400f;

        // Lógica de Conexão Manual e Desconexão
        config(Integer.class, (LinearShieldBuild tile, Integer pos) -> {
            Building other = Vars.world.build(pos);

            // Se clicar no próprio poste, desconecta tudo dele
            if (other == tile) {
                for (int i = 0; i < tile.links.size; i++) {
                    Building linked = Vars.world.build(tile.links.get(i));
                    if (linked instanceof LinearShieldBuild ob) ob.links.removeValue(tile.pos());
                }
                tile.links.clear();
            }
            // Conecta/Desconecta bidirecionalmente de um poste vizinho
            else if (other instanceof LinearShieldBuild ob && ob.team == tile.team) {
                if (tile.links.contains(pos)) {
                    tile.links.removeValue(pos);
                    ob.links.removeValue(tile.pos());
                } else if (tile.links.size < maxLinks && ob.links.size < maxLinks && tile.dst(ob) <= shieldRange) {
                    tile.links.addUnique(pos);
                    ob.links.addUnique(tile.pos());
                }
            }
        });
    }

    // PRÉ-VISUALIZAÇÃO DE CONEXÃO AO COLOCAR (Ghost block)
    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        float cx = x * Vars.tilesize + offset;
        float cy = y * Vars.tilesize + offset;

        // Círculo de alcance pontilhado
        Drawf.dashCircle(cx, cy, shieldRange, Pal.accent);

        LinearShieldBuild closest = null;
        float minDist = shieldRange;

        // Procura o aliado mais próximo
        for (LinearShieldBuild other : activeShields) {
            if (other.team != Vars.player.team()) continue;
            float d = Mathf.dst(cx, cy, other.x, other.y);
            if (d <= minDist && other.links.size < maxLinks) {
                minDist = d;
                closest = other;
            }
        }

        // Desenha a linha de pré-conexão
        if (closest != null) {
            Drawf.square(closest.x, closest.y, closest.block.size * Vars.tilesize / 2f + 1f, Pal.place);
            Lines.stroke(2f, Pal.place);
            Lines.line(cx, cy, closest.x, closest.y);
        }
    }

    public class LinearShieldBuild extends Building {
        public IntSeq links = new IntSeq();
        public float overloadTimer = 0f, warmup = 0f, buildup = 0f, hit = 0f;
        public boolean isOverloading = false, broken = false;

        @Override
        public void add() {
            super.add();
            activeShields.addUnique(this);
        }

        // AUTO-CONEXÃO AO SER CONSTRUÍDO
        @Override
        public void placed() {
            super.placed();
            LinearShieldBuild closest = null;
            float minDist = shieldRange;

            for (LinearShieldBuild other : activeShields) {
                if (other == this || other.team != team) continue;
                float d = dst(other);
                if (d <= minDist && other.links.size < maxLinks) {
                    minDist = d;
                    closest = other;
                }
            }

            if (closest != null) {
                configure(closest.pos());
            }
        }

        @Override
        public void drawConfigure() {
            super.drawConfigure();
            Drawf.dashCircle(x, y, shieldRange, Pal.accent);

            for (int i = 0; i < links.size; i++) {
                Building linked = Vars.world.build(links.get(i));
                if (linked != null) {
                    Drawf.square(linked.x, linked.y, linked.block.size * Vars.tilesize / 2f + 1f, Pal.place);
                }
            }
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if (this == other) {
                configure(pos()); // Clicou em si mesmo = limpa
                deselect();
                return false;
            }
            if (other instanceof LinearShieldBuild && dst(other) <= shieldRange) {
                configure(other.pos()); // Clicou em outro = conecta/desconecta
                return false;
            }
            return super.onConfigureBuildTapped(other);
        }

        @Override
        public void onRemoved() {
            activeShields.remove(this);
            for (int i = 0; i < links.size; i++) {
                Building other = Vars.world.build(links.get(i));
                if (other instanceof LinearShieldBuild b) b.links.removeValue(pos());
            }
            super.onRemoved();
        }

        private boolean isValidLink(Building b) {
            return b instanceof LinearShieldBuild && b.team == this.team && b.isValid();
        }

        @Override
        public void updateTile() {
            hit = Mathf.lerpDelta(hit, 0f, 0.1f);

            if (broken) {
                buildup -= delta() * cooldownBrokenBase;
                if (buildup <= 0f) { broken = false; buildup = 0f; }
                warmup = Mathf.lerpDelta(warmup, 0f, 0.05f);
            } else {
                buildup -= delta() * cooldownNormal;
                if (buildup < 0f) buildup = 0f;
                warmup = Mathf.lerpDelta(warmup, potentialEfficiency > 0 ? 1f : 0f, 0.05f);
            }

            if (buildup >= shieldHealth && !broken) {
                broken = true;
                buildup = shieldHealth;
                Fx.shieldBreak.at(x, y);
            }

            if (isOverloading) {
                overloadTimer += Time.delta;
                if (overloadTimer >= 60f) executeOverload();
            }

            // A LÓGICA DO "ENGOLIDOR DE BOMBAS" (E balas normais)
            if (!broken && warmup > 0.5f) {
                for (int i = 0; i < links.size; i++) {
                    Building linked = Vars.world.build(links.get(i));

                    // Apenas um lado calcula a colisão para economizar CPU
                    if (isValidLink(linked) && linked.pos() > pos()) {
                        LinearShieldBuild b = (LinearShieldBuild) linked;
                        if (b.broken) continue;

                        float thickness = 14f;
                        Groups.bullet.intersect(Math.min(x, linked.x) - thickness, Math.min(y, linked.y) - thickness, Math.abs(x - linked.x) + thickness * 2, Math.abs(y - linked.y) + thickness * 2, bullet -> {

                            if (bullet.team != team) {
                                boolean hitLine = Intersector.distanceLinePoint(x, y, linked.x, linked.y, bullet.x, bullet.y) <= thickness + bullet.type.hitSize / 2f;
                                if (!hitLine) hitLine = Intersector.intersectSegments(bullet.x - bullet.vel.x, bullet.y - bullet.vel.y, bullet.x, bullet.y, x, y, linked.x, linked.y, null);

                                if (hitLine) {
                                    // A MÁGICA: Se o tiro for a Bomba Nuclear
                                    if (bullet.type instanceof SiloNuclear.NukeBulletType) {
                                        bullet.data = "absorvido"; // Avisa a bomba para NÃO explodir (Fica inofensiva)
                                        bullet.remove(); // Apaga ela do jogo fisicamente
                                        triggerOverload(); // Aciona a destruição em cadeia do Escudo
                                    }
                                    // Se for tiro normal (Titanium, Surge, etc)
                                    else if (bullet.type.absorbable) {
                                        bullet.absorb();
                                        hit = b.hit = 1f;
                                        buildup += bullet.damage / 2f;
                                        b.buildup += bullet.damage / 2f;
                                        Fx.absorb.at(bullet);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }

        public void triggerOverload() {
            if (isOverloading) return;
            isOverloading = true;
            overloadTimer = 0f;
        }

        // EXPLOSÃO EM CADEIA
        private void executeOverload() {
            this.kill();
            Fx.massiveExplosion.at(x, y);
            for (int i = 0; i < links.size; i++) {
                Building other = Vars.world.build(links.get(i));
                if (isValidLink(other)) ((LinearShieldBuild) other).triggerOverload();
            }
        }

        @Override
        public void draw() {
            super.draw();
            if (warmup <= 0.01f || broken) return;

            Draw.z(Layer.shields);
            for (int i = 0; i < links.size; i++) {
                Building linked = Vars.world.build(links.get(i));
                if (isValidLink(linked) && linked.pos() > pos()) {
                    LinearShieldBuild b = (LinearShieldBuild) linked;
                    if (b.broken) continue;

                    float angle = Angles.angle(x, y, linked.x, linked.y);

                    // ESCUDO MAIS FORTE: Opacidade base 75%
                    float alpha = (0.75f + 0.25f * Math.max(hit, b.hit)) * warmup;

                    Draw.color(team.color);

                    // Camada 1: Fundo (Glow Largo e suave)
                    Draw.alpha(alpha * 0.4f);
                    Lines.stroke(16f);
                    Lines.line(x, y, linked.x, linked.y);

                    // Camada 2: Núcleo (Sólido com a cor do time)
                    Draw.alpha(alpha * 0.8f);
                    Lines.stroke(8f);
                    Lines.line(x, y, linked.x, linked.y);

                    // Camada 3: Feixe Central (Branco Puro para aspecto de laser)
                    Draw.color(Color.white);
                    Draw.alpha(alpha);
                    Lines.stroke(3f);
                    Lines.line(x, y, linked.x, linked.y);

                    // Conexões estéticas
                    Draw.color(team.color);
                    Draw.alpha(alpha);
                    Lines.stroke(3f);
                    Tmp.v1.trns(angle + 90, 8);
                    Lines.line(x + Tmp.v1.x, y + Tmp.v1.y, linked.x + Tmp.v1.x, linked.y + Tmp.v1.y);
                    Tmp.v1.trns(angle - 90, 8);
                    Lines.line(x + Tmp.v1.x, y + Tmp.v1.y, linked.x + Tmp.v1.x, linked.y + Tmp.v1.y);
                }
            }

            // Animação visual de que o escudo vai explodir
            if (isOverloading) {
                Draw.color(Color.red);
                Draw.alpha(Mathf.absin(4f, 1f));
                Fill.circle(x, y, block.size * 4f + Mathf.absin(2f, 4f));
            }
            Draw.reset();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(links.size);
            for (int i = 0; i < links.size; i++) write.i(links.get(i));
            write.f(buildup);
            write.bool(broken);
            write.bool(isOverloading);
            write.f(overloadTimer);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int count = read.i();
            for (int i = 0; i < count; i++) links.addUnique(read.i());
            buildup = read.f();
            broken = read.bool();
            isOverloading = read.bool();
            overloadTimer = read.f();
        }
    }
}