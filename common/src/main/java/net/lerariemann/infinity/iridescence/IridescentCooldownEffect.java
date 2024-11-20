package net.lerariemann.infinity.iridescence;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;

public class IridescentCooldownEffect extends StatusEffect {
    public IridescentCooldownEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        entity.addStatusEffect(new StatusEffectInstance(Iridescence.getEffect(ModStatusEffects.IRIDESCENT_COOLDOWN),
                Iridescence.getCooldownDuration(), 0));
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return amplifier != 0 && duration < 2;
    }
}
