package nuclearmod.blocks.defense;

import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import nuclearmod.content.ModItems;
import nuclearmod.content.blocks.ModPayloadBlocks;

public class SiloNuclear extends ItemTurret {

    public SiloNuclear(String name) {
        super(name);
        acceptsPayload = true;
    }

    public class SiloNuclearBuild extends ItemTurretBuild {

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            if (payload instanceof BuildPayload bp) {
                return bp.block() == ModPayloadBlocks.missileComplete && totalAmmo < maxAmmo;
            }
            return false;
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            for (int i = 0; i < 10; i++) {
                handleItem(this, ModItems.uranioEnriquecido);
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
                    Bullet b = bulletType.create(this, this.team, this.x, this.y, 0f, 0f, 1f);

                    if (b != null) {
                        bulletType.hit(b, this.x, this.y);
                        b.remove();
                    }
                }
            }
            super.onDestroyed();
        }
    }
}
