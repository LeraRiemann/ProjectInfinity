package net.lerariemann.infinity.mixin.iridescence;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @WrapOperation(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isFrozen()Z"))
    boolean inj(LivingEntity entity, Operation<Boolean> original) {
        var frozen = original.call(entity);
        if (entity instanceof MobEntity ent && Iridescence.isConvertible(ent)) {
            return (frozen || Iridescence.isUnderEffect(ent));
        }
        return frozen;
    }
}
