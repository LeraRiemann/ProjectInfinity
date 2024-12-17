package net.lerariemann.infinity.mixin.iridescence;

import net.lerariemann.infinity.registry.core.ModStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectInstance.class)
public abstract class StatusEffectInstanceMixin {
    @Shadow public abstract RegistryEntry<StatusEffect> getEffectType();

    @Shadow private int duration;

    @Shadow private int amplifier;

    /* Hook for unconventional status effect ticking logic */
    @Inject(method = "update", at = @At(target = "Lnet/minecraft/entity/effect/StatusEffectInstance;updateDuration()I", value = "INVOKE"))
    void inj(LivingEntity entity, Runnable overwriteCallback, CallbackInfoReturnable<Boolean> cir) {
        if (getEffectType().value() instanceof ModStatusEffects.SpecialEffect eff) {
            eff.tryApplySpecial(entity, duration, amplifier);
        }
    }
}
