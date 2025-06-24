package net.lerariemann.infinity.options;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;
import java.util.Random;

public interface PortalColorApplier {
    static PortalColorApplier of(Identifier id, MinecraftServer server) {
        return of(id, InfinityOptions.readData(server, id));
    }
    static PortalColorApplier of(Identifier id, NbtCompound defaultData) {
        NbtCompound data = InfinityMod.provider.easterizer.optionmap.get(id.getPath());
        if (data == null) data = defaultData;
        return of(data, (int)InfinityMethods.getNumericFromId(id));
    }
    static PortalColorApplier of(NbtCompound data, int defaultColor) {
        if (!data.contains("portal_color")) return new PortalColorApplier.Simple(defaultColor);
        if (data.contains("portal_color", NbtElement.INT_TYPE)) return new PortalColorApplier.Simple(NbtUtils.getInt(data, "portal_color"));
        NbtCompound applierData = NbtUtils.getCompound(data, "portal_color");
        return switch (NbtUtils.getString(applierData, "type", "")) {
            case "simple" -> new PortalColorApplier.Simple(NbtUtils.getInt(applierData, "value"));
            case "checker" -> new PortalColorApplier.Checker(applierData.getList("values", NbtElement.INT_TYPE));
            case "random_hue" -> new PortalColorApplier.RandomHue(applierData);
            case "random" -> PortalColorApplier.RandomColor.INSTANCE;
            default -> new PortalColorApplier.Simple(defaultColor);
        };
    }

    int apply(BlockPos pos);

    record Simple(int value) implements PortalColorApplier {
        @Override
        public int apply(BlockPos pos) {
            return value;
        }
    }

    record Checker(NbtList values) implements PortalColorApplier {
        @Override
        public int apply(BlockPos pos) {
            int mod = InfinityMethods.properMod(pos.getX() + pos.getY() + pos.getZ(), values.size());
            return values.getInt(mod);
        }
    }

    enum RandomColor implements PortalColorApplier {
        INSTANCE;
        @Override
        public int apply(BlockPos pos) {
            return (new Random(pos.hashCode())).nextInt();
        }
    }

    record RandomHue(float saturation, float brightness, float detail) implements PortalColorApplier {
        public RandomHue(NbtCompound applierData) {
            this(NbtUtils.getFloat(applierData, "saturation", 1.0f),
                    NbtUtils.getFloat(applierData, "brightness", 1.0f),
                    NbtUtils.getFloat(applierData, "detail", 12.0f));
        }

        @Override
        public int apply(BlockPos pos) {
            float hue = (pos.getX() + pos.getY() + pos.getZ()) / detail;
            return Color.HSBtoRGB(hue - (int)hue, saturation, brightness) & 0xFFFFFF;
        }
    }
}
