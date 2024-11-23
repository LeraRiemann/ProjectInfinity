package net.lerariemann.infinity.mixin.iridescence;

import net.lerariemann.infinity.iridescence.ModStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "onStatusEffectsRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;updateAttributes()V"))
    void inj(Collection<StatusEffectInstance> effects, CallbackInfo ci) {
        for (StatusEffectInstance effect : effects) {
            if (effect.getEffectType().value() instanceof ModStatusEffects.SpecialEffect eff) {
                eff.onRemoved((LivingEntity)(Object)this);
            }
        }
    }
}
