package net.lerariemann.infinity.mixin.iridescence;

import net.lerariemann.infinity.iridescence.ModStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "onStatusEffectRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getAttributes()Lnet/minecraft/entity/attribute/AttributeContainer;"))
    void inj(StatusEffectInstance effect, CallbackInfo ci) {
        if (effect.getEffectType() instanceof ModStatusEffects.SpecialEffect eff) {
            eff.onRemoved((LivingEntity)(Object)this);
        }
    }
}
