package net.lerariemann.infinity.iridescence;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;

public class IridescentCooldownEffect extends StatusEffect {
    public IridescentCooldownEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        entity.addStatusEffect(new StatusEffectInstance(ModStatusEffects.IRIDESCENT_COOLDOWN,
                Iridescence.getCooldownDuration(), 0, false, false, false));
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return amplifier != 0 && duration < 2;
    }
}
