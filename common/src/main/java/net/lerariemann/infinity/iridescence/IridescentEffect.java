package net.lerariemann.infinity.iridescence;

import net.lerariemann.infinity.util.WarpLogic;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.awt.*;

public class IridescentEffect extends StatusEffect implements ModStatusEffects.SpecialEffect {
    public IridescentEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);
        if (entity.hasStatusEffect(ModStatusEffects.IRIDESCENT_SETUP)) {
            entity.removeStatusEffect(ModStatusEffects.IRIDESCENT_SETUP);
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

    @Override
    public ParticleEffect createParticle(StatusEffectInstance effect) {
        float hue = effect.getDuration() / 13.0f;
        return EntityEffectParticleEffect.create(
                ParticleTypes.ENTITY_EFFECT, ColorHelper.Argb.withAlpha(255,
                        Color.HSBtoRGB(hue - (int)hue, 1.0f, 1.0f)));
    }
}
