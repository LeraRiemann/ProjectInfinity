package net.lerariemann.infinity.iridescence;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;

public class IridescentEffect extends StatusEffect {
    protected IridescentEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);
        //entity.addStatusEffect(new StatusEffectInstance(ModStatusEffects.IRIDESCENCE_COOLDOWN, 168000, 1));
    }

    public void onRemoved(LivingEntity entity) {
        entity.setInvulnerable(false);
    }

}
