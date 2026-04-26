package nuclearmod.blocks;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.type.StatusEffect;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import nuclearmod.content.ModFx;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;

public class SiloNuclear extends ItemTurret {

    // =========================================================
    // EFEITO VISUAL: Fogo Azul (Maior, denso e duradouro)
    // =========================================================
    public static final Effect virusBlueFire = new Effect(90f, e -> { // Tempo dobrado para 90f
        Draw.color(Color.valueOf("00aaff"), Color.valueOf("0044cc"), Color.darkGray, e.fin());
        // Aumentado para 5 vetores e raio de dispersão maior (10f)
        Angles.randLenVectors(e.id, 5, 2f + e.fin() * 10f, (x, y) -> {
            // Círculos maiores
            Fill.circle(e.x + x, e.y + y, 0.8f + e.fout() * 2.5f);
        });
    });

    // =========================================================
    // 1. O VÍRUS DO FOGO NA CONSTRUÇÃO
    // =========================================================
    public static class ViralFire extends BulletType {
        public ViralFire() {
            super(0f, 0f);
            lifetime = 18000f;
            pierce = true;
            pierceBuilding = true;
            collides = false;
            hittable = false;
            absorbable = false;
            drawSize = 0f;
        }

        @Override
        public void draw(Bullet b) {}

        @Override
        public void update(Bullet b) {
            super.update(b);
            Tile tile = Vars.world.tileWorld(b.x, b.y);

            if (tile == null || tile.build == null) {
                b.remove();
                return;
            }

            // -------------------------------------------------------------
            // ENGANANDO A IA: Fogo Tradicional para atrair as torres de água!
            // -------------------------------------------------------------
            // A cada mais ou menos 1 segundo, ele gera um fogo comum no bloco.
            // O fogo azul é grande o suficiente para engolir o visual dele,
            // mas é o suficiente para as torres Wave e Tsunami mirarem no bloco!
            if (Mathf.chanceDelta(0.075f)) {
                mindustry.entities.Fires.create(tile);
            }

            // -------------------------------------------------------------
            // A CURA: Água ou Criogênio matam o vírus
            // -------------------------------------------------------------
            mindustry.gen.Puddle poca = mindustry.entities.Puddles.get(tile);
            // Reduzi a exigência de amount para 0.2f (Bem mais sensível à água das torres!)
            if (poca != null && poca.liquid.temperature <= 0.5f && poca.amount > 0.2f) {
                b.remove(); // Vírus apagado com sucesso!
                return;
            }

            // -------------------------------------------------------------
            // VISUAL E DANO (Separados)
            // -------------------------------------------------------------
            if (Mathf.chanceDelta(0.3f)) {
                virusBlueFire.at(tile.worldx() + Mathf.range(6f), tile.worldy() + Mathf.range(6f));
            }

            if (Mathf.chanceDelta(0.01f)) {
                tile.build.damage(2f);
            }

            // -------------------------------------------------------------
            // CONTAMINAÇÃO DE TROPAS TERRESTRES
            // -------------------------------------------------------------
            if (Mathf.chanceDelta(0.05f)) {
                Units.nearby(b.x - 8f, b.y - 8f, 16f, 16f, u -> {
                    if (!u.hasEffect(StatusEffects.wet) && !u.hasEffect(StatusEffects.freezing)) {
                        u.apply(virusAzulUnidades, 300f);
                    }
                });
            }

            // -------------------------------------------------------------
            // PULSO DE CONTÁGIO ENTRE BLOCOS
            // -------------------------------------------------------------
            if (Mathf.chanceDelta(0.030f)) {
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dy = -3; dy <= 3; dy++) {
                        if (dx == 0 && dy == 0) continue;

                        if (Mathf.chance(0.01f)) {
                            Tile vizinho = Vars.world.tile(tile.x + dx, tile.y + dy);
                            if (vizinho != null && vizinho.build != null) {
                                boolean[] jaInfectado = {false};
                                Groups.bullet.intersect(vizinho.worldx() - 1f, vizinho.worldy() - 1f, 2f, 2f, other -> {
                                    if (other.type == this) jaInfectado[0] = true;
                                });

                                if (!jaInfectado[0]) {
                                    Call.createBullet(this, b.team, vizinho.worldx(), vizinho.worldy(), 0f, 0f, 1f, 1f);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Instância única do Fogo Viral para ser referenciada
    public static ViralFire fogoViral = new ViralFire();

    // =========================================================
    // 2. STATUS EFFECT: O Vírus nas Unidades
    // =========================================================
    public static final StatusEffect virusAzulUnidades = new StatusEffect("virus-azul-unidades") {
        {
            color = Color.valueOf("00aaff");
            damage = 0.2f; // Dano contínuo enquanto a tropa está infectada
            effect = virusBlueFire;
            effectChance = 0.15f;
        }

        @Override
        public void update(mindustry.gen.Unit unit, mindustry.entities.units.StatusEntry entry) {
            super.update(unit, entry); // Passando o novo objeto entry para o super

            // A cura: Se a unidade passar na água, o vírus some
            if (unit.hasEffect(StatusEffects.wet) || unit.hasEffect(StatusEffects.freezing)) {
                unit.unapply(this);
            }

            // O Rastro: Unidade infectada espalha o fogo para as construções por onde passa
            if (Mathf.chanceDelta(0.1f)) {
                Tile t = unit.tileOn();
                if (t != null && t.build != null) {
                    boolean[] jaInfectado = {false};
                    Groups.bullet.intersect(t.worldx() - 1f, t.worldy() - 1f, 2f, 2f, other -> {
                        if (other.type instanceof ViralFire) jaInfectado[0] = true;
                    });
                    if (!jaInfectado[0]) {
                        Call.createBullet(fogoViral, unit.team, t.worldx(), t.worldy(), 0f, 0f, 1f, 1f);
                    }
                }
            }
        }
    };

    // =========================================================
    // 3. FAÍSCAS (Transportam o vírus para as bordas)
    // =========================================================
    public static class NukeSpark extends BasicBulletType {
        public ViralFire viralType;

        public NukeSpark(ViralFire viralType) {
            super(8f, 0f);
            this.viralType = viralType;
            this.lifetime = 55f;
            this.trailColor = Color.valueOf("00aaff");
            this.trailWidth = 3f;
            this.trailLength = 20;
            this.frontColor = Color.white;
            this.backColor = Color.valueOf("0044cc");
            this.hitEffect = Fx.hitFlameSmall;
            this.despawnEffect = Fx.none;
            this.collidesAir = false;
            fragVelocityMin = 0.8f;
            fragVelocityMax = 1.5f;
        }

        @Override
        public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct) {
            super.hitTile(b, build, x, y, initialHealth, direct);
            if (build != null) {
                Call.createBullet(viralType, b.team, build.x, build.y, 0f, 0f, 1f, 1f);
            }
        }

        @Override
        public void hitEntity(Bullet b, mindustry.gen.Hitboxc entity, float initialHealth) {
            super.hitEntity(b, entity, initialHealth);
            if (entity instanceof Building build) {
                Call.createBullet(viralType, b.team, build.x, build.y, 0f, 0f, 1f, 1f);
            }
        }
    }

    public static NukeSpark faiscaNuke = new NukeSpark(fogoViral);


    // =========================================================
    // 4. A BOMBA PRINCIPAL
    // =========================================================
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

            int originTx = Math.round(ex / Vars.tilesize);
            int originTy = Math.round(ey / Vars.tilesize);

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

            for (int dx = -raioTotalBlocos; dx <= raioTotalBlocos; dx++) {
                for (int dy = -raioTotalBlocos; dy <= raioTotalBlocos; dy++) {
                    float distBlocos = Mathf.dst(0, 0, dx, dy);
                    if (distBlocos > raioTotalBlocos) continue;

                    Tile tile = Vars.world.tile(tx + dx, ty + dy);
                    if (tile == null) continue;

                    float targetX = tile.worldx();
                    float targetY = tile.worldy();
                    boolean isShielded = false;

                    for (ShieldSegment seg : activeSegments) {
                        if (Intersector.intersectSegments(ex, ey, targetX, targetY, seg.s1.x, seg.s1.y, seg.s2.x, seg.s2.y, null)) {
                            isShielded = true;
                            break;
                        }
                    }

                    if (!isShielded) {
                        int targetTx = tile.x;
                        int targetTy = tile.y;

                        boolean hitNaturalWall = Vars.world.raycast(originTx, originTy, targetTx, targetTy, (rx, ry) -> {
                            if (rx == targetTx && ry == targetTy) return false;

                            Tile checkTile = Vars.world.tile(rx, ry);
                            if (checkTile != null && checkTile.solid() && checkTile.build == null) {
                                return true;
                            }
                            return false;
                        });

                        if (hitNaturalWall) {
                            isShielded = true;
                        }
                    }

                    if (!isShielded) {
                        if (tile.build != null) {
                            if (distBlocos <= 28f) alvosCriticos.addUnique(tile.build);
                            else alvosSeveros.addUnique(tile.build);
                        }

                        if (dx % 2 == 0 && dy % 2 == 0) {
                            ModFx.nukeScorch.at(targetX, targetY);
                        }

                        // === O SEU ANEL DE FOGO VOLTOU AQUI! ===
                        if (distBlocos > 20f && Mathf.chance(0.8f)) {
                            mindustry.entities.Puddles.deposit(tile, mindustry.content.Liquids.oil, 10000f);
                            mindustry.entities.Fires.create(tile);
                        }
                        // =======================================
                    }
                }
            }

            for(Building alvo : alvosCriticos) alvo.damage(100000f);
            for(Building alvo : alvosSeveros) alvo.damage(100f);

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
                    int targetTx = Math.round(u.x / Vars.tilesize);
                    int targetTy = Math.round(u.y / Vars.tilesize);

                    boolean hitNaturalWall = Vars.world.raycast(originTx, originTy, targetTx, targetTy, (rx, ry) -> {
                        if (rx == targetTx && ry == targetTy) return false;
                        Tile checkTile = Vars.world.tile(rx, ry);
                        if (checkTile != null && checkTile.solid() && checkTile.build == null) {
                            return true;
                        }
                        return false;
                    });

                    if (hitNaturalWall) unitShielded = true;
                }

                if (!unitShielded) {
                    float dist = u.dst(ex, ey) / Vars.tilesize;
                    if(dist <= 25f) u.kill();
                    else {
                        u.damage(1000f);
                        u.apply(StatusEffects.burning, 9999999f);
                    }
                }
            });

            for (int i = 0; i < 70; i++) {
                float angle = Mathf.random(360f);
                float lifeScale = Mathf.random(0.7f, 1.3f);
                Call.createBullet(faiscaNuke, b.team, ex, ey, angle, 1f, lifeScale, 1f);
            }
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
            // O míssil completo vale 10 unidades de munição para encher o silo de uma vez!
            for (int i = 0; i < 10; i++) {
                handleItem(this, nuclearmod.content.ModItems.uranioEnriquecido);
            }
        }

        @Override
        protected void shoot(BulletType type) {
            Call.sendMessage("[scarlet]⚠ ALERTA NUCLEAR:\n[white]Míssil lançado!");
            super.shoot(type);
        }
        @Override
        public void onDestroyed() {
            if (this.hasAmmo()) {
                var bulletType = peekAmmo();
                if (bulletType != null) {
                    // O penúltimo parâmetro "0f" é o multiplicador de velocidade (a bala não sai do lugar)
                    // O último parâmetro "1f" é o multiplicador de vida
                    Bullet b = bulletType.create(this, this.team, this.x, this.y, 0f, 0f, 1f);

                    if (b != null) {
                        // Força a bala a chamar a detonação no exato pixel e depois a remove do mapa
                        bulletType.hit(b, this.x, this.y);
                        b.remove();
                    }
                }
            }
            super.onDestroyed();
        }

    }
}