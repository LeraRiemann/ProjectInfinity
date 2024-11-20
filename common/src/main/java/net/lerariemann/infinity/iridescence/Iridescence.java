package net.lerariemann.infinity.iridescence;

import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.PlatformMethods;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class Iridescence {
    public static boolean isInfinite(World world) {
        return world.getRegistryKey().getValue().toString().equals("infinity:chaos");
    }
    public static boolean isIridescence(FluidState st) {
        return st.isOf(PlatformMethods.getIridescenceStill().get()) || st.isOf(PlatformMethods.getIridescenceFlowing().get());
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

    public enum Phase {
        INITIAL,
        UPWARDS,
        PLATEAU,
        DOWNWARDS
    }

    public static RegistryEntry<StatusEffect> getEffect(RegistrySupplier<StatusEffect> supplier) {
        Optional<RegistryKey<StatusEffect>> opt = supplier.getKeyOrValue().left();
        assert opt.isPresent();
        return Registries.STATUS_EFFECT.entryOf(opt.get());
    }
}
