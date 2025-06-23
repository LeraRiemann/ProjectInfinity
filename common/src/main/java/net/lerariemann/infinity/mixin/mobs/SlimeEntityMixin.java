package net.lerariemann.infinity.mixin.mobs;

import net.lerariemann.infinity.entity.custom.ChaosSlime;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SlimeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.lerariemann.infinity.entity.custom.ChaosSlime.color;

@Mixin(SlimeEntity.class)
public class SlimeEntityMixin {
    /* Allows chaos slimes to correctly inherit their additional data when split. */
    // TODO 1.21.3 check if this still works
    @Inject(method = "method_63653", at = @At(value = "RETURN"))
    private void injected(int i, float f, float g, SlimeEntity instance, CallbackInfo ci) {
        SlimeEntity e = ((SlimeEntity)(Object)(this));
        if (e instanceof ChaosSlime slime_mom) {
            ChaosSlime slime_son = (ChaosSlime)instance;
            slime_son.setCore(slime_mom.getCoreForChild());
            slime_son.setColor(slime_mom.getDataTracker().get(color));
        }
    }
}
