package net.lerariemann.infinity.iridescence;

import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.entity.custom.ChaosCreeper;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.var.ModCriteria;
import net.lerariemann.infinity.var.ModPayloads;
import net.lerariemann.infinity.var.ModStats;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Iridescence {
    public static boolean isInfinite(World world) {
        return world.getRegistryKey().getValue().toString().equals("infinity:chaos");
    }
    public static boolean isIridescence(FluidState st) {
        return st.isOf(PlatformMethods.getIridescenceStill().get()) || st.isOf(PlatformMethods.getIridescenceFlowing().get());
    }
    public static boolean isIridescence(WorldView world, BlockPos pos) {
        return Iridescence.isIridescence(world.getFluidState(pos));
    }

    public static boolean isUnderEffect(LivingEntity entity) {
        return entity.hasStatusEffect(ModStatusEffects.IRIDESCENT_EFFECT);
    }

    public static int color(BlockPos pos) {
        int i = pos.getX() + pos.getY() + pos.getZ();
        return Color.HSBtoRGB(i / 600.0f + (float)((Math.sin(pos.getX()/12.0f) + Math.sin(pos.getZ()/12.0f)) / 4), 1.0F, 1.0F);
    }

    public static java.util.List<String> colors = List.of("minecraft:white_",
            "minecraft:red_",
            "minecraft:orange_",
            "minecraft:yellow_",
            "minecraft:lime_",
            "minecraft:green_",
            "minecraft:cyan_",
            "minecraft:light_blue_",
            "minecraft:blue_",
            "minecraft:purple_",
            "minecraft:magenta_",
            "minecraft:pink_",
            "minecraft:gray_",
            "minecraft:light_gray_",
            "minecraft:black_",
            "minecraft:brown_");

    public static Block getRandomColorBlock(WorldAccess world, String str) {
        return Registries.BLOCK.get(Identifier.of(colors.get(world.getRandom().nextInt(16)) + str));
    }
    public static Block getRandomColorBlock(double d, String str) {
        return Registries.BLOCK.get(Identifier.of(colors.get((int)(d*16)) + str));
    }

    public static final int ticksInHour = 1000;

    public static int getAmplifierOnApply(LivingEntity entity, int original) {
        StatusEffectInstance cooldown = entity.getStatusEffect(ModStatusEffects.IRIDESCENT_COOLDOWN);
        if (cooldown == null) return original;
        else if (cooldown.getAmplifier() < 1) return 0;
        return -1;
    }

    public static int getEffectLength(int amplifier) {
        return ticksInHour * (3 + 2*amplifier);
    }

    public static int getCooldownDuration() {
        return ticksInHour * 24 * 7;
    }

    public static Phase getPhase(int duration, int amplifier) {
        int effect_length = getEffectLength(amplifier) - ticksInHour;
        int time_passed = effect_length - duration;
        if (time_passed < 0) return Phase.INITIAL;
        return (time_passed < ticksInHour) ? Phase.UPWARDS : (duration <= ticksInHour || amplifier == 0) ? Phase.DOWNWARDS : Phase.PLATEAU;
    }

    public static boolean shouldWarp(int duration, int amplifier) {
        return (Iridescence.getPhase(duration, amplifier) == Iridescence.Phase.PLATEAU) && (duration % ticksInHour == 0);
    }

    public static boolean shouldReturn(int duration, int amplifier) {
        return (amplifier > 0) && (duration == ticksInHour);
    }

    public static boolean shouldUpdateShader(int duration, int amplifier) {
        return getEffectLength(amplifier) - duration == ticksInHour;
    }

    public static void updateShader(ServerPlayerEntity player) {
        PlatformMethods.sendS2CPayload(player, ModPayloads.setShaderFromWorld(player.getServerWorld()));
    }

    @Nullable
    public static Identifier shouldApplyShader(@Nullable ClientPlayerEntity player) {
        if (player == null) return null;
        StatusEffectInstance effect = player.getStatusEffect(ModStatusEffects.IRIDESCENT_EFFECT);
        if (effect == null) return null;
        return (getPhase(effect.getDuration(), effect.getAmplifier()) != Phase.INITIAL) ?
                InfinityMod.getId("shaders/post/iridescence.json") :
                null;
    }

    public static void tryBeginJourney(LivingEntity entity, int amplifier) {
        int amplifier1 = Iridescence.getAmplifierOnApply(entity, amplifier);
        if (amplifier1 >= 0) {
            entity.addStatusEffect(new StatusEffectInstance(ModStatusEffects.IRIDESCENT_EFFECT,
                    Iridescence.getEffectLength(amplifier1), amplifier1));
            entity.removeStatusEffect(ModStatusEffects.IRIDESCENT_COOLDOWN);
            entity.addStatusEffect(new StatusEffectInstance(ModStatusEffects.IRIDESCENT_COOLDOWN,
                    Iridescence.getCooldownDuration(), amplifier1 > 0 ? 1 : 0, false, false, false));
            if (entity instanceof ServerPlayerEntity player) {
                player.increaseStat(ModStats.IRIDESCENCE, 1);
                ModCriteria.IRIDESCENT.get().trigger(player);
            }
        }
    }

    public static Identifier getIdForWarp(ServerPlayerEntity player) {
        ServerWorld w = player.getServerWorld().getServer().getOverworld();
        return InfinityMod.getDimId(new Random(w.getSeed() + w.getTime() / ticksInHour).nextInt());
    }

    public static final Map<EntityType<? extends MobEntity>, RegistrySupplier<? extends EntityType<? extends MobEntity>>> convertibles =
            Map.ofEntries(Map.entry(EntityType.SKELETON, ModEntities.CHAOS_SKELETON),
            Map.entry(EntityType.CREEPER, ModEntities.CHAOS_CREEPER),
            Map.entry(EntityType.SLIME, ModEntities.CHAOS_SLIME)
    );

    public static boolean isConvertible(MobEntity entity) {
        return (convertibles.containsKey(entity.getType()) || (entity instanceof ChaosPawn pawn && pawn.isChess()));
    }

    public static EntityType<? extends MobEntity> getConversion(MobEntity entity) {
        return convertibles.get(entity.getType()).get();
    }

    public static void tryBeginConversion(MobEntity ent) {
        if (isConvertible(ent) && !ent.hasStatusEffect(ModStatusEffects.IRIDESCENT_EFFECT))
            ent.addStatusEffect(new StatusEffectInstance(ModStatusEffects.IRIDESCENT_EFFECT, ticksInHour, 0));
    }

    public static void endConversion(MobEntity currEntity) {
        EntityType<? extends MobEntity> typeNew = Iridescence.getConversion(currEntity);
        if (typeNew != null) {
            MobEntity newEntity = typeNew.create(currEntity.getWorld());
            if (newEntity != null) {
                currEntity.discard();
                ModEntities.copy(currEntity, newEntity);
                if (currEntity instanceof SlimeEntity e1 && newEntity instanceof SlimeEntity e2) {
                    e1.setSize(e2.getSize(), true);
                }
                if (newEntity instanceof ChaosCreeper creeper) {
                    RegistryEntry<Biome> b = creeper.getWorld().getBiome(creeper.getBlockPos());
                    creeper.setBiome(b.getIdAsString());
                    creeper.setColor(b.value().getFoliageColor());
                }
                currEntity.getWorld().spawnEntity(newEntity);
                newEntity.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.0f);
            }
        }
    }

    public enum Phase {
        INITIAL,
        UPWARDS,
        PLATEAU,
        DOWNWARDS
    }
}
