package net.lerariemann.infinity.iridescence;

import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.registry.core.ModEntities;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.registry.core.ModStatusEffects;
import net.lerariemann.infinity.registry.var.ModCriteria;
import net.lerariemann.infinity.registry.var.ModStats;
import net.lerariemann.infinity.registry.var.ModTags;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.entity.custom.ChaosCreeper;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.core.RandomProvider;
import net.lerariemann.infinity.util.teleport.WarpLogic;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public interface Iridescence {
    Identifier TEXTURE = InfinityMethods.getId("block/iridescence");
    Identifier FLOWING_TEXTURE = InfinityMethods.getId("block/iridescence");
    Identifier OVERLAY_TEXTURE = InfinityMethods.getId("block/iridescence_overlay");
    DoublePerlinNoiseSampler sampler =
            DoublePerlinNoiseSampler.create(new CheckedRandom(0L), -5, genOctaves(2));

    static double[] genOctaves(int octaves){
        double[] a = new double[octaves];
        Arrays.fill(a, 1);
        return a;
    }

    static double sample(BlockPos pos) {
        return sampler.sample(pos.getX(), pos.getY(), pos.getZ());
    }

    static boolean isInfinite(World world) {
        return switch (world.getRegistryKey().getValue().toString()) {
            case "infinity:chaos", "infinity:colors" -> true;
            default -> false;
        };
    }
    static boolean isIridescence(FluidState st) {
        return st.isOf(PlatformMethods.getIridescenceStill().get()) || st.isOf(PlatformMethods.getIridescenceFlowing().get());
    }
    static boolean isIridescence(BlockView world, BlockPos pos) {
        return Iridescence.isIridescence(world.getFluidState(pos));
    }
    static boolean isIridescentItem(ItemStack stack) {
        return stack.isIn(ModTags.IRIDESCENT_ITEMS);
    }

    static boolean isUnderEffect(LivingEntity entity) {
        return entity.hasStatusEffect(ModStatusEffects.IRIDESCENT_EFFECT);
    }

    static int getPosBasedColor(BlockPos pos) {
        return Color.HSBtoRGB((float)sample(pos), 1.0F, 1.0F) & 0xFFFFFF;
    }
    static int getTimeBasedColor() {
        long timeMS = LocalTime.now().toNanoOfDay() / 1000000;
        int hue = (int)(timeMS % 24000);
        return Color.HSBtoRGB((float)(hue / 24000.0), 1.0f, 1.0f) & 0xFFFFFF;
    }

    java.util.List<DyeColor> dyeColors = List.of(
            DyeColor.RED,
            DyeColor.ORANGE,
            DyeColor.YELLOW,
            DyeColor.LIME,
            DyeColor.GREEN,
            DyeColor.CYAN,
            DyeColor.LIGHT_BLUE,
            DyeColor.BLUE,
            DyeColor.PURPLE,
            DyeColor.MAGENTA,
            DyeColor.PINK);

    static Block getRandomColorBlock(WorldAccess world, String str) {
        return Registries.BLOCK.get(Identifier.of(dyeColors.get(world.getRandom().nextInt(dyeColors.size())).getName() + "_" + str));
    }
    static Block getRandomColorBlock(double d, String str) {
        return Registries.BLOCK.get(Identifier.of(dyeColors.get((int)(d* dyeColors.size())).getName() + "_" + str));
    }

    static int getAmplifierOnApply(LivingEntity entity, int original) {
        StatusEffectInstance cooldown = entity.getStatusEffect(ModStatusEffects.IRIDESCENT_COOLDOWN);
        if (cooldown == null) return original;
        else if (cooldown.getAmplifier() < 1) return 0;
        return -1;
    }

    int ticksInHour = 1200;

    static int getOnsetLength() {
        return ticksInHour * RandomProvider.ruleInt("iridescenceInitialDuration") / 60; //default is 30 seconds
    }
    static int getFullEffectLength(int amplifier) {
        return getOnsetLength() + getEffectLength(amplifier);
    }
    static int getEffectLength(int amplifier) { //amplifier is 0 to 4
        return getComeupLength() + getPeakLength(amplifier) + getOffsetLength(); //8 to 12 minutes
    }
    static int getComeupLength() {
        return ticksInHour;
    }
    static int getPeakLength(int amplifier) {
        return ticksInHour * (4 + amplifier);
    }
    static int getOffsetLength() {
        return ticksInHour * 3;
    }
    static int getAfterglowDuration() {
        return ticksInHour * RandomProvider.ruleInt("afterglowDuration"); //default is 24 minutes
    }
    static int getCooldownDuration() {
        return ticksInHour * RandomProvider.ruleInt("iridescenceCooldownDuration"); //default is 7*24 minutes
    }

    static Phase getPhase(LivingEntity entity) {
        StatusEffectInstance effect = entity.getStatusEffect(ModStatusEffects.IRIDESCENT_EFFECT);
        if (effect == null) return Phase.INITIAL;
        return getPhase(effect.getDuration(), effect.getAmplifier());
    }
    static Phase getPhase(int duration, int amplifier) {
        int time_passed = getEffectLength(amplifier) - duration;
        if (time_passed < 0) return Phase.INITIAL;
        return (time_passed < getComeupLength()) ? Phase.UPWARDS : (duration <= getOffsetLength() || amplifier == 0) ? Phase.DOWNWARDS : Phase.PLATEAU;
    }

    static boolean shouldWarp(int duration, int amplifier) {
        return (Iridescence.getPhase(duration, amplifier) == Iridescence.Phase.PLATEAU) && (duration % ticksInHour == 0);
    }
    static boolean shouldReturn(int duration, int amplifier) {
        return (amplifier > 0) && (duration == ticksInHour);
    }
    static boolean shouldRequestShaderLoad(int duration, int amplifier) {
        int time_passed = getEffectLength(amplifier) - duration;
        return (time_passed == 0);
    }

    static void loadShader(ServerPlayerEntity player) {
        InfinityMethods.sendS2CPayload(player, ModPayloads.setShaderFromWorld(player.getServerWorld(), true));
    }
    static void unloadShader(ServerPlayerEntity player) {
        InfinityMethods.sendS2CPayload(player, ModPayloads.setShaderFromWorld(player.getServerWorld(), false));
    }

    static boolean shouldApplyShader(@Nullable PlayerEntity player) {
        if (player == null) return false;
        StatusEffectInstance effect = player.getStatusEffect(ModStatusEffects.IRIDESCENT_EFFECT);
        return (effect != null && effect.getDuration() > 20
                && getPhase(effect.getDuration(), effect.getAmplifier()) != Phase.INITIAL);
    }

    static void tryBeginJourney(LivingEntity entity, int amplifier, boolean willingly) {
        amplifier = Iridescence.getAmplifierOnApply(entity, amplifier);
        if (amplifier >= 0) {
            entity.addStatusEffect(new StatusEffectInstance(ModStatusEffects.IRIDESCENT_EFFECT,
                    Iridescence.getFullEffectLength(amplifier),
                    amplifier, true, true));
            entity.removeStatusEffect(ModStatusEffects.IRIDESCENT_COOLDOWN);
            int cooldownDuration = Iridescence.getCooldownDuration();
            if (cooldownDuration > 0)
                entity.addStatusEffect(new StatusEffectInstance(ModStatusEffects.IRIDESCENT_COOLDOWN,
                        cooldownDuration, amplifier > 0 ? 1 : 0, true, false));
            if (entity instanceof ServerPlayerEntity player) {
                ModCriteria.IRIDESCENT.get().trigger(player, willingly, amplifier);
            }
        }
    }

    static void saveCookie(ServerPlayerEntity player) {
        NbtCompound compound = new NbtCompound();
        compound.putDouble("x", player.getPos().x);
        compound.putDouble("y", player.getPos().y);
        compound.putDouble("z", player.getPos().z);
        compound.putString("dim", player.getServerWorld().getRegistryKey().getValue().toString());
        CommonIO.write(compound, InfinityMod.provider.savingPath, player.getUuidAsString() + ".json");
    }

    static void endJourney(ServerPlayerEntity player, boolean isEarlyCancel, int amplifier) {
        player.setInvulnerable(false);
        if (!isEarlyCancel) {
            player.increaseStat(ModStats.IRIDESCENCE, 1);
            if (amplifier != 0)
                player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.AFTERGLOW,
                        getAfterglowDuration(), 0, true, true));
        }
        Path cookie = InfinityMod.provider.savingPath.resolve(player.getUuidAsString() + ".json");
        try {
            NbtCompound comp = CommonIO.read(cookie);
            player.teleport(player.server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(comp.getString("dim")))),
                    comp.getDouble("x"), comp.getDouble("y"), comp.getDouble("z"), player.getYaw(), player.getPitch());
        } catch (Exception e) {
            WarpLogic.respawnAlive(player);
        }
        try {
            Files.deleteIfExists(cookie);
        } catch (Exception ignored) {
        }
    }

    static Identifier getIdForWarp(ServerPlayerEntity player) {
        ServerWorld w = player.getServerWorld().getServer().getOverworld();
        return InfinityMethods.getRandomId(new Random(w.getSeed() + w.getTime() / ticksInHour));
    }

    Map<EntityType<? extends MobEntity>, RegistrySupplier<? extends EntityType<? extends MobEntity>>> convertibles =
            Map.ofEntries(Map.entry(EntityType.SKELETON, ModEntities.CHAOS_SKELETON),
            Map.entry(EntityType.CREEPER, ModEntities.CHAOS_CREEPER),
            Map.entry(EntityType.SLIME, ModEntities.CHAOS_SLIME)
    );

    static boolean isConvertible(MobEntity entity) {
        return (convertibles.containsKey(entity.getType()) || (entity instanceof ChaosPawn pawn && pawn.isBlackOrWhite()));
    }

    static void tryApplyEffect(MobEntity ent) {
        if (!ent.hasStatusEffect(ModStatusEffects.IRIDESCENT_EFFECT)) {
            if (ent instanceof FishEntity)
                ent.addStatusEffect(new StatusEffectInstance(ModStatusEffects.IRIDESCENT_EFFECT, ticksInHour, 0,
                        true, false));
            else if (isConvertible(ent))
                ent.addStatusEffect(new StatusEffectInstance(ModStatusEffects.IRIDESCENT_EFFECT, ticksInHour, 0,
                        true, true));
        }
    }

    static void endConversion(MobEntity currEntity) {
        EntityType<?> type = currEntity.getType();
        if (!convertibles.containsKey(type)) return;
        EntityType<? extends MobEntity> typeNew = convertibles.get(type).get();
        if (typeNew != null) {
            MobEntity newEntity = typeNew.create(currEntity.getWorld());
            if (newEntity != null) {
                currEntity.discard();
                ModEntities.copy(currEntity, newEntity);
                if (newEntity instanceof ChaosCreeper creeper) {
                    RegistryEntry<Biome> b = creeper.getWorld().getBiome(creeper.getBlockPos());
                    creeper.setBiome(b.getIdAsString());
                    creeper.setColor(b.value().getFoliageColor());
                    creeper.setRandomCharge();
                }
                else if (currEntity.getWorld() instanceof ServerWorld w)
                    newEntity.initialize(w, w.getLocalDifficulty(currEntity.getBlockPos()), SpawnReason.CONVERSION, null);
                if (currEntity instanceof SlimeEntity e1 && newEntity instanceof SlimeEntity e2) {
                    e2.setSize(e1.getSize(), true);
                }
                currEntity.getWorld().spawnEntity(newEntity);
                newEntity.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.0f);
                convTriggers(currEntity);
            }
        }
    }

    static void convTriggers(LivingEntity entity) {
        triggerConversion(entity.getWorld().getClosestPlayer(entity.getX(), entity.getY(), entity.getZ(),
                50, false), entity);
        entity.getWorld().getPlayers(TargetPredicate.DEFAULT, entity, Box.of(entity.getPos(), 10,10, 10))
                .forEach(p -> triggerConversion(p, entity));
    }

    static void triggerConversion(PlayerEntity player, LivingEntity entity) {
        if (player instanceof ServerPlayerEntity np) {
            ModCriteria.CONVERT_MOB.get().trigger(np, entity);
        }
    }

    enum Phase {
        INITIAL,
        UPWARDS,
        PLATEAU,
        DOWNWARDS
    }
}
