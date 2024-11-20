package net.lerariemann.infinity.iridescence;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;

public class IridescentSetupEffect extends StatusEffect {
    public IridescentSetupEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);
        int i = Iridescence.getAmplifierOnApply(entity, amplifier);
        if (i >= 0) entity.addStatusEffect(new StatusEffectInstance(ModStatusEffects.IRIDESCENT_EFFECT,
                    Iridescence.getEffectLength(amplifier), i));
    }
}
