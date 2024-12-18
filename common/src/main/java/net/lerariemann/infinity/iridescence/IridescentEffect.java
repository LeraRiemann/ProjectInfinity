package net.lerariemann.infinity.iridescence;

import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.util.WarpLogic;
import net.lerariemann.infinity.registry.core.ModStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.awt.Color;

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
        if (entity instanceof Angerable ang) ang.stopAnger();
        if (entity instanceof ServerPlayerEntity player
                && Iridescence.shouldApplyShader(player))
            Iridescence.loadShader(player);
    }

    public void onRemoved(LivingEntity entity) {
        entity.setInvulnerable(false);
        switch (entity) {
            case ServerPlayerEntity player -> Iridescence.unloadShader(player);
            case ChaosPawn pawn -> {
                if (pawn.getRandom().nextBoolean()) {
                    pawn.unchess();
                    Iridescence.convTriggers(pawn);
                }
            }
            case MobEntity currEntity -> Iridescence.endConversion(currEntity);
            default -> {
            }
        }
    }

    @Override
    public void tryApplySpecial(LivingEntity entity, int duration, int amplifier) {
        if (entity instanceof ServerPlayerEntity player) {
            if (Iridescence.shouldWarp(duration, amplifier)) {
                player.setInvulnerable(true);
                Identifier id = Iridescence.getIdForWarp(player);
                WarpLogic.requestWarp(player, id, false);
            }
            if (Iridescence.shouldReturn(duration, amplifier)) {
                player.setInvulnerable(false);
                WarpLogic.respawnAlive(player);
            }
            if (Iridescence.shouldRequestShaderLoad(duration, amplifier))
                Iridescence.loadShader(player);
        }
    }

    @Override
    public ParticleEffect createParticle(StatusEffectInstance effect) {
        float hue = effect.getDuration() / 13.0f;
        return EntityEffectParticleEffect.create(
                ParticleTypes.ENTITY_EFFECT, ColorHelper.Argb.withAlpha(255,
                        Color.HSBtoRGB(hue - (int)hue, 1.0f, 1.0f)));
    }

    public static class Setup extends InstantStatusEffect {
        public Setup(StatusEffectCategory category, int color) {
            super(category, color);
        }

        @Override
        public void onApplied(LivingEntity entity, int amplifier) {
            super.onApplied(entity, amplifier);
            Iridescence.tryBeginJourney(entity, amplifier);
        }
    }
}
