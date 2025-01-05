package net.lerariemann.infinity.mixin.iridescence;

import net.lerariemann.infinity.registry.core.ModStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(StatusEffectUtil.class)
public class StatusEffectUtilMixin {
    @Inject(method = "hasHaste", at = @At("RETURN"), cancellable = true)
    private static void injected(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.hasStatusEffect(ModStatusEffects.AFTERGLOW.value()))
            cir.setReturnValue(true);
    }

    @Inject(method = "getHasteAmplifier", at = @At("RETURN"), cancellable = true)
    private static void inj2(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (entity.hasStatusEffect(ModStatusEffects.AFTERGLOW.value()))
            cir.setReturnValue(cir.getReturnValue() +
                    Objects.requireNonNull(entity.getStatusEffect(ModStatusEffects.AFTERGLOW.value())).getAmplifier());
    }
}
