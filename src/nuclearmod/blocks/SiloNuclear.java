package nuclearmod.blocks;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
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

        @Override
        public void update(Bullet b) {
            super.update(b);
            float lastX = b.x - b.vel.x;
            float lastY = b.y - b.vel.y;
            Vec2 intersect = new Vec2();

            for (LinearShieldProjector.LinearShieldBuild shield : LinearShieldProjector.activeShields) {
                if (shield.broken || shield.warmup <= 0.5f) continue;

                if (shield.efficiency > 0) {
                    for (int i = 0; i < shield.links.size; i++) {
                        Building linked = Vars.world.build(shield.links.get(i));

                        if (linked instanceof LinearShieldProjector.LinearShieldBuild bLinked && linked.pos() > shield.pos()) {
                            // CORREÇÃO: Garante que a outra ponta da linha também está viva e com escudo
                            if (bLinked.broken || bLinked.warmup <= 0.5f) continue;

                            if (Intersector.intersectSegments(lastX, lastY, b.x, b.y, shield.x, shield.y, linked.x, linked.y, intersect)) {
                                float recuoX = intersect.x - (b.vel.x * 0.1f);
                                float recuoY = intersect.y - (b.vel.y * 0.1f);
                                detonacaoNuclear(b, recuoX, recuoY);
                                b.remove();
                                return;
                            }
                        }
                    }
                }
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
            boolean hitShield = false;

            for (LinearShieldProjector.LinearShieldBuild shield : LinearShieldProjector.activeShields) {
                if (shield.broken || shield.warmup <= 0.5f) continue;

                for (int i = 0; i < shield.links.size; i++) {
                    Building linked = Vars.world.build(shield.links.get(i));

                    if (linked instanceof LinearShieldProjector.LinearShieldBuild bLinked && linked.pos() > shield.pos()) {
                        if (bLinked.broken || bLinked.warmup <= 0.5f) continue;

                        if (Intersector.distanceLinePoint(shield.x, shield.y, linked.x, linked.y, ex, ey) < 40f) {
                            shield.triggerOverload();
                            hitShield = false;
                            break;
                        }
                    }
                }
                if (hitShield) break;
            }

            ModFx.nukeExplosion.at(ex, ey);
            Effect.shake(60f, 60f, ex, ey);

            if (hitShield) {
                Units.nearbyEnemies(b.team, ex - 200, ey - 200, 400, 400, u -> {
                    u.damage(1000f);
                });
                return;
            }

            int tx = (int)(ex / Vars.tilesize);
            int ty = (int)(ey / Vars.tilesize);
            int raioTotalBlocos = 40;

            Seq<Building> alvosCriticos = new Seq<>();
            Seq<Building> alvosSeveros = new Seq<>();

            for (int dx = -raioTotalBlocos; dx <= raioTotalBlocos; dx++) {
                for (int dy = -raioTotalBlocos; dy <= raioTotalBlocos; dy++) {
                    float distBlocos = Mathf.dst(0, 0, dx, dy);
                    if (distBlocos > raioTotalBlocos) continue;

                    Tile tile = Vars.world.tile(tx + dx, ty + dy);
                    if (tile == null) continue;

                    float targetX = tile.worldx();
                    float targetY = tile.worldy();
                    boolean isShielded = false;

                    for (LinearShieldProjector.LinearShieldBuild shield : LinearShieldProjector.activeShields) {
                        if (shield.broken || shield.warmup <= 0.5f) continue;

                        for (int i = 0; i < shield.links.size; i++) {
                            Building linked = Vars.world.build(shield.links.get(i));

                            if (linked instanceof LinearShieldProjector.LinearShieldBuild bLinked && linked.pos() > shield.pos()) {
                                if (bLinked.broken || bLinked.warmup <= 0.5f) continue;

                                if (Intersector.intersectSegments(ex, ey, targetX, targetY, shield.x, shield.y, linked.x, linked.y, null)) {
                                    shield.triggerOverload();
                                    isShielded = true;
                                    break;
                                }
                            }
                        }
                        if (isShielded) break;
                    }

                    if (!isShielded) {
                        if (tile.build != null) {
                            if (distBlocos <= 28f) alvosCriticos.add(tile.build);
                            else alvosSeveros.add(tile.build);
                        }
                        if (distBlocos > 20f && Mathf.chance(0.8f)) Fires.create(tile);
                    }
                }
            }

            for(Building alvo : alvosCriticos) alvo.damage(100000f);
            for(Building alvo : alvosSeveros) alvo.damage(20000f);

            float raioTotalPixels = raioTotalBlocos * Vars.tilesize;
            Units.nearby(ex - raioTotalPixels, ey - raioTotalPixels, raioTotalPixels * 2, raioTotalPixels * 2, u -> {
                boolean unitShielded = false;

                for (LinearShieldProjector.LinearShieldBuild shield : LinearShieldProjector.activeShields) {
                    if (shield.broken || shield.warmup <= 0.5f) continue;

                    for (int i = 0; i < shield.links.size; i++) {
                        Building linked = Vars.world.build(shield.links.get(i));

                        if (linked instanceof LinearShieldProjector.LinearShieldBuild bLinked && linked.pos() > shield.pos()) {
                            if (bLinked.broken || bLinked.warmup <= 0.5f) continue;

                            if (Intersector.intersectSegments(ex, ey, u.x, u.y, shield.x, shield.y, linked.x, linked.y, null)) {
                                shield.triggerOverload();
                                unitShielded = true;
                                break;
                            }
                        }
                    }
                    if (unitShielded) break;
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