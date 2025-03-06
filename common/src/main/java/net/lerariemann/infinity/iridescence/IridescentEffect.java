package net.lerariemann.infinity.iridescence;

import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.util.teleport.WarpLogic;
import net.lerariemann.infinity.registry.core.ModStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.Objects;

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
        if (Objects.requireNonNull(entity) instanceof PlayerEntity player) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                unloadShader(serverPlayer);
                if (player.isInvulnerable()) endJourney(serverPlayer, true, 0);
            }
        } else if (entity instanceof ChaosPawn pawn) {
            if (pawn.getRandom().nextBoolean()) {
                pawn.unchess();
                pawn.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.0f);
                convTriggers(pawn);
            }
        } else if (entity instanceof MobEntity currEntity) {
            endConversion(currEntity);
        }
    }

    @Override
    public void tryApplySpecial(LivingEntity entity, int duration, int amplifier) {
        if (entity instanceof PlayerEntity player) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                if (shouldWarp(duration, amplifier)) {
                    if (!player.isInvulnerable()) {
                        player.setInvulnerable(true);
                        saveCookie(serverPlayer);
                    }
                    Identifier id = getIdForWarp(serverPlayer);
                    WarpLogic.requestWarp(serverPlayer, id, false);
                }
                if (shouldReturn(duration, amplifier)) {
                    endJourney(serverPlayer, false, amplifier);
                }
                if (shouldRequestShaderLoad(duration, amplifier))
                    loadShader(serverPlayer);
            }
            if (amplifier == 0 && duration == 2) {
                player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.AFTERGLOW.value(),
                        getAfterglowDuration() / 2, 0, true, true));
            }
        }
    }

    public static class Setup extends InstantStatusEffect {
        public Setup(StatusEffectCategory category, int color) {
            super(category, color);
        }

        @Override
        public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
            super.onApplied(entity, attributes, amplifier);
            tryBeginJourney(entity, amplifier, true);
        }
    }
}