package net.lerariemann.infinity.iridescence;

import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.util.teleport.WarpLogic;
import net.lerariemann.infinity.registry.core.ModStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.lerariemann.infinity.iridescence.Iridescence.*;

public class IridescentEffect extends StatusEffect implements ModStatusEffects.SpecialEffect {
    public IridescentEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (entity.hasStatusEffect(ModStatusEffects.IRIDESCENT_SETUP.value())) {
            entity.removeStatusEffect(ModStatusEffects.IRIDESCENT_SETUP.value());
        }
        if (entity instanceof Angerable ang) ang.stopAnger();
        if (entity instanceof ServerPlayerEntity player
                && shouldApplyShader(player))
            loadShader(player);
    }

    public void onRemoved(LivingEntity entity) {
        switch (entity) {
            case ServerPlayerEntity player -> {
                unloadShader(player);
                if (player.isInvulnerable()) endJourney(player, true, 0);
            }
            case ChaosPawn pawn -> {
                if (pawn.getRandom().nextBoolean()) {
                    pawn.unchess();
                    convTriggers(pawn);
                }
            }
            case MobEntity currEntity -> endConversion(currEntity);
            default -> {
            }
        } else if (entity instanceof MobEntity currEntity) {
            Iridescence.endConversion(currEntity);
        }
    }

    @Override
    public void tryApplySpecial(LivingEntity entity, int duration, int amplifier) {
        if (entity instanceof ServerPlayerEntity player) {
            if (shouldWarp(duration, amplifier)) {
                if (!player.isInvulnerable()) {
                    player.setInvulnerable(true);
                    saveCookie(player);
                }
                Identifier id = getIdForWarp(player);
                WarpLogic.requestWarp(player, id, false);
            }
            if (shouldReturn(duration, amplifier)) {
                endJourney(player, false, amplifier);
            }
            if (shouldRequestShaderLoad(duration, amplifier))
                loadShader(player);
            if (amplifier == 0 && duration == 2) {
                player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.AFTERGLOW,
                        getAfterglowDuration() / 2, 0, true, true));
            }
        }
    }

//    @Override
//    public ParticleEffect createParticle(StatusEffectInstance effect) {
//        float hue = effect.getDuration() / 13.0f;
//        return EntityEffectParticleEffect.create(
//                ParticleTypes.ENTITY_EFFECT, ColorHelper.Argb.withAlpha(255,
//                        Color.HSBtoRGB(hue - (int)hue, 1.0f, 1.0f)));
//    }

    public static class Setup extends InstantStatusEffect {
        public Setup(StatusEffectCategory category, int color) {
            super(category, color);
        }

        @Override
        public void onApplied(LivingEntity entity, int amplifier) {
            super.onApplied(entity, amplifier);
            tryBeginJourney(entity, amplifier);
        }
    }
}