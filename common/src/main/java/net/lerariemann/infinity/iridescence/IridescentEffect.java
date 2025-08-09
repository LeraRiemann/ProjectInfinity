package net.lerariemann.infinity.iridescence;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.util.core.RandomProvider;
import net.lerariemann.infinity.util.loading.ShaderLoader;
import net.lerariemann.infinity.util.teleport.WarpLogic;
import net.lerariemann.infinity.registry.core.ModStatusEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.awt.Color;

import static net.lerariemann.infinity.iridescence.Iridescence.*;

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
                && shouldApplyShader(player))
            loadShader(player);
    }

    public void onRemoved(LivingEntity entity) {
        switch (entity) {
            case PlayerEntity player -> {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    if (player.isInvulnerable()) player.setInvulnerable(false);
                    if (Iridescence.getPhase(entity) == Iridescence.Phase.PLATEAU) endJourney(serverPlayer, true, 0);
                    unloadShader(serverPlayer);
                }
                else {
                    ShaderLoader.reloadShaders(MinecraftClient.getInstance(), InfinityOptions.ofClient().data, false);
                }
            }
            case ChaosPawn pawn -> {
                if (pawn.getRandom().nextBoolean()) {
                    pawn.unchess();
                    pawn.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.0f);
                    convTriggers(pawn);
                }
            }
            case MobEntity currEntity -> endConversion(currEntity);
            default -> {
            }
        }
    }

    @Override
    public void tryApplySpecial(LivingEntity entity, int duration, int amplifier) {
        if (entity instanceof PlayerEntity p) {
            if (p instanceof ServerPlayerEntity player) {
                if (shouldWarp(duration, amplifier)) {
                    if (Iridescence.getPhase(duration + ticksInHour, amplifier) != Iridescence.Phase.PLATEAU) { //the first warp
                        if (RandomProvider.rule("iridSafeMode")) player.setInvulnerable(true);
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
            else {
                Iridescence.updateAtomics(duration, amplifier);
                if (amplifier == 0) return;
                double prog = ShaderLoader.iridProgress.get();
                if (prog > 0.5) {
                    if (InfinityMod.random.nextDouble() < 0.015*(2*prog - 1)*amplifier)
                        entity.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1f + InfinityMod.random.nextFloat());
                }
            }
        }
    }

    @Override
    public ParticleEffect createParticle(StatusEffectInstance effect) {
        float hue = effect.getDuration() / 13.0f;
        return EntityEffectParticleEffect.create(
                ParticleTypes.ENTITY_EFFECT, ColorHelper.Argb.fullAlpha(Color.HSBtoRGB(hue - (int)hue, 1.0f, 1.0f)));
    }

    public static class Setup extends InstantStatusEffect {
        public Setup(StatusEffectCategory category, int color) {
            super(category, color);
        }

        @Override
        public void onApplied(LivingEntity entity, int amplifier) {
            super.onApplied(entity, amplifier);
            tryBeginJourney(entity, amplifier, true);
        }
    }
}
