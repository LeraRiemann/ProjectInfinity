package net.lerariemann.infinity.fluid;

import net.lerariemann.infinity.PlatformMethods;
import net.minecraft.block.Block;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.awt.*;
import java.util.List;

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
}
