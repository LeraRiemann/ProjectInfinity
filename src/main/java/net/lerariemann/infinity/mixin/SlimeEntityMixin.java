package net.lerariemann.infinity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.entity.custom.DimensionalSlime;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SlimeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.lerariemann.infinity.entity.custom.DimensionalSlime.color;

@Mixin(SlimeEntity.class)
public class SlimeEntityMixin {
    @Inject(method = "remove(Lnet/minecraft/entity/Entity$RemovalReason;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/mob/SlimeEntity;setAiDisabled(Z)V"))
    private void injected(Entity.RemovalReason reason, CallbackInfo ci, @Local SlimeEntity slimeEntity) {
        SlimeEntity e = ((SlimeEntity)(Object)(this));
        if (((SlimeEntity)(Object)(this)) instanceof DimensionalSlime) {
            DimensionalSlime slime_mom = (DimensionalSlime)e;
            DimensionalSlime slime_son = (DimensionalSlime)slimeEntity;
            slime_son.setCore(slime_mom.getCore());
            slime_son.setColor(slime_mom.getDataTracker().get(color));
        }
    }
}
