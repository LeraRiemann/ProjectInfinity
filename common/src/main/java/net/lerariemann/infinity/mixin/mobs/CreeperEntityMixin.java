package net.lerariemann.infinity.mixin.mobs;

import net.lerariemann.infinity.entity.custom.ChaosCreeper;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin {
    @Shadow
    private void spawnEffectsCloud(){}

    /* A hook for chaos creepers' explosion logic. */
    @Inject(method = "explode()V", at = @At("HEAD"), cancellable = true)
    private void injected(CallbackInfo ci) {
        if (((CreeperEntity)(Object)this) instanceof ChaosCreeper) {
            ChaosCreeper e = (ChaosCreeper)(Object)this;
            World w = e.getWorld();
            if (!w.isClient) {
                e.blow_up();
                spawnEffectsCloud();
                ci.cancel();
            }
        }
    }
}
