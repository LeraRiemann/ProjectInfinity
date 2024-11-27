package net.lerariemann.infinity.options;

import net.lerariemann.infinity.util.WarpLogic;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.awt.*;
import java.util.Objects;

public abstract class PortalColorApplier {
    public abstract int apply(Identifier id, MinecraftServer server, BlockPos pos);

    public static class Empty extends PortalColorApplier {
        @Override
        public int apply(Identifier id, MinecraftServer server, BlockPos pos) {
            return (int)WarpLogic.getNumericFromId(id, server);
        }
    }

    public static class Simple extends PortalColorApplier {
        public int value;

        Simple(int i) {
            value = i;
        }

        @Override
        public int apply(Identifier id, MinecraftServer server, BlockPos pos) {
            return value;
        }
    }

    public static class Checker extends PortalColorApplier {
        public NbtList values;

        Checker(NbtList lst) {
            values = lst;
        }

        @Override
        public int apply(Identifier id, MinecraftServer server, BlockPos pos) {
            int mod = WarpLogic.properMod(pos.getX() + pos.getY() + pos.getZ(), values.size());
            return values.getInt(mod);
        }
    }

    net.minecraft.util.math.random.Random randomExtract(MinecraftServer server) {
        return Objects.requireNonNull(server.getWorld(World.OVERWORLD)).getRandom();
    }

    public static class Random extends PortalColorApplier {
        @Override
        public int apply(Identifier id, MinecraftServer server, BlockPos pos) {
            return randomExtract(server).nextInt(16777216);
        }
    }

    public static class RandomHue extends PortalColorApplier {
        float saturation;
        float brightness;

        RandomHue(NbtCompound applierData) {
            saturation = InfinityOptions.test(applierData, "saturation", 1.0f);
            brightness = InfinityOptions.test(applierData, "brightness", 1.0f);
        }

        @Override
        public int apply(Identifier id, MinecraftServer server, BlockPos pos) {
            return Color.HSBtoRGB(randomExtract(server).nextFloat(), saturation, brightness);
        }
    }
}
