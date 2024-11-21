package net.lerariemann.infinity.iridescence;

import net.lerariemann.infinity.util.WarpLogic;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class IridescentEffect extends StatusEffect implements ModStatusEffects.SpecialEffect {
    public IridescentEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (entity.hasStatusEffect((StatusEffect) ModStatusEffects.IRIDESCENT_SETUP)) {
            entity.removeStatusEffect((StatusEffect) ModStatusEffects.IRIDESCENT_SETUP);
        }
    }

    public void onRemoved(LivingEntity entity) {
        entity.setInvulnerable(false);
        if (entity instanceof ServerPlayerEntity player) {
            Iridescence.updateShader(player);
        }
    }

    @Override
    public void tryApplySpecial(LivingEntity entity, int duration, int amplifier) {
        if (entity instanceof ServerPlayerEntity player) {
            if (Iridescence.shouldWarp(duration, amplifier)) {
                player.setInvulnerable(true);
                Identifier id = WarpLogic.getRandomId(player.getServer(), player.getRandom());
                WarpLogic.warpWithTimer(player, id, 10, false);
            }
            if (Iridescence.shouldReturn(duration, amplifier)) {
                player.setInvulnerable(false);
                WarpLogic.respawnAlive(player);
            }
            if (Iridescence.shouldUpdateShader(duration)) {
                Iridescence.updateShader(player);
            }
        }
    }
}
