package nuclearmod.blocks;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Fires;
import mindustry.entities.Units;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import nuclearmod.content.ModFx;
import arc.math.geom.Intersector;

public class SiloNuclear extends ItemTurret {

    public static class NukeBulletType extends ArtilleryBulletType {
        public NukeBulletType() {
            super(2f, 100000f);
            this.hitEffect = ModFx.nukeExplosion;
            this.despawnEffect = ModFx.nukeExplosion;
            this.sprite = "shell";
            this.lifetime = 2000f;
            this.width = 14f;
            this.height = 18f;
            this.trailWidth = 6f;
            this.trailLength = 35;
            this.trailColor = Color.valueOf("ff7a38");
            this.trailEffect = Fx.missileTrailSmoke;
            this.trailInterval = 2f;
            this.trailParam = 4f;
            this.collides = true;
            this.collidesAir = true;
            this.collidesGround = false;
            this.collidesTeam = true;
            this.hitShake = 50f;
        }

        // Classe auxiliar interna para otimizar o cálculo de colisão da onda de choque
        private static class ShieldSegment {
            LinearShieldProjector.LinearShieldBuild s1, s2;
            ShieldSegment(LinearShieldProjector.LinearShieldBuild s1, LinearShieldProjector.LinearShieldBuild s2) {
                this.s1 = s1; this.s2 = s2;
            }
        }

        @Override
        public void hit(Bullet b, float x, float y) {
            super.hit(b, x, y);
            detonacaoNuclear(b, x, y);
        }

        @Override
        public void despawned(Bullet b) {
            super.despawned(b);
            detonacaoNuclear(b, b.x, b.y);
        }

        private void detonacaoNuclear(Bullet b, float ex, float ey) {
            ModFx.nukeExplosion.at(ex, ey);
            Effect.shake(60f, 60f, ex, ey);

            // PRÉ-CÁLCULO: Filtra apenas segmentos de escudos ativos e conectados
            Seq<ShieldSegment> activeSegments = new Seq<>();
            for (LinearShieldProjector.LinearShieldBuild shield : LinearShieldProjector.activeShields) {
                if (shield.broken || shield.warmup <= 0.5f) continue;
                for (int i = 0; i < shield.links.size; i++) {
                    Building linked = Vars.world.build(shield.links.get(i));
                    if (linked instanceof LinearShieldProjector.LinearShieldBuild bLinked && linked.pos() > shield.pos()) {
                        if (bLinked.broken || bLinked.warmup <= 0.5f) continue;
                        activeSegments.add(new ShieldSegment(shield, bLinked));
                    }
                }
            }

            int tx = (int)(ex / Vars.tilesize);
            int ty = (int)(ey / Vars.tilesize);
            int raioTotalBlocos = 40;

            Seq<Building> alvosCriticos = new Seq<>();
            Seq<Building> alvosSeveros = new Seq<>();

            // Loop de Tiles (Onda de Calor e Choque)
            for (int dx = -raioTotalBlocos; dx <= raioTotalBlocos; dx++) {
                for (int dy = -raioTotalBlocos; dy <= raioTotalBlocos; dy++) {
                    float distBlocos = Mathf.dst(0, 0, dx, dy);
                    if (distBlocos > raioTotalBlocos) continue;

                    Tile tile = Vars.world.tile(tx + dx, ty + dy);
                    if (tile == null) continue;

                    float targetX = tile.worldx();
                    float targetY = tile.worldy();
                    boolean isShielded = false;

                    // Verifica se há um escudo entre a explosão e este tile específico
                    for (ShieldSegment seg : activeSegments) {
                        if (Intersector.intersectSegments(ex, ey, targetX, targetY, seg.s1.x, seg.s1.y, seg.s2.x, seg.s2.y, null)) {
                            seg.s1.triggerOverload(); // O escudo absorve o impacto da onda
                            isShielded = true;
                            break;
                        }
                    }

                    if (!isShielded) {
                        if (tile.build != null) {
                            if (distBlocos <= 28f) alvosCriticos.addUnique(tile.build);
                            else alvosSeveros.addUnique(tile.build);
                        }
                        if (distBlocos > 20f && Mathf.chance(0.8f)) Fires.create(tile);
                    }
                }
            }

            // Aplica dano massivo
            for(Building alvo : alvosCriticos) alvo.damage(100000f);
            for(Building alvo : alvosSeveros) alvo.damage(20000f);

            // Dano em Unidades
            float raioPixels = raioTotalBlocos * Vars.tilesize;
            Units.nearby(ex - raioPixels, ey - raioPixels, raioPixels * 2, raioPixels * 2, u -> {
                boolean unitShielded = false;
                for (ShieldSegment seg : activeSegments) {
                    if (Intersector.intersectSegments(ex, ey, u.x, u.y, seg.s1.x, seg.s1.y, seg.s2.x, seg.s2.y, null)) {
                        unitShielded = true;
                        break;
                    }
                }

                if (!unitShielded) {
                    float dist = u.dst(ex, ey) / Vars.tilesize;
                    if(dist <= 25f) u.kill();
                    else {
                        u.damage(15000f);
                        u.apply(StatusEffects.burning, 900f);
                    }
                }
            });
        }
    }

    public SiloNuclear(String name) {
        super(name);
        acceptsPayload = true;
    }

    public class SiloNuclearBuild extends ItemTurretBuild {
        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            if (payload instanceof BuildPayload bp) {
                return bp.block() == nuclearmod.content.ModBlocks.missileComplete && totalAmmo < maxAmmo;
            }
            return false;
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            handleItem(this, nuclearmod.content.ModItems.uranioEnriquecido);
        }

        @Override
        protected void shoot(BulletType type) {
            Call.sendMessage("[scarlet]⚠ ALERTA NUCLEAR:\n[white]Míssil lançado!");
            super.shoot(type);
        }
    }
}