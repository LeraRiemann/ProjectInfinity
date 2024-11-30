package net.lerariemann.infinity.options;

import net.lerariemann.infinity.util.WarpLogic;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.Random;

public interface PortalColorApplier {
    static PortalColorApplier extract(NbtCompound data, int def) {
        if (!data.contains("portal_color")) return new PortalColorApplier.Simple(def);
        if (data.contains("portal_color", NbtElement.INT_TYPE)) return new PortalColorApplier.Simple(data.getInt("portal_color"));
        NbtCompound applierData = data.getCompound("portal_color");
        return switch (applierData.getString("type")) {
            case "simple" -> new PortalColorApplier.Simple(applierData.getInt("value"));
            case "checker" -> new PortalColorApplier.Checker(applierData.getList("values", NbtElement.INT_TYPE));
            case "random_hue" -> new PortalColorApplier.RandomHue(applierData);
            case "random" -> PortalColorApplier.RandomColor.INSTANCE;
            default -> new PortalColorApplier.Simple(def);
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
            int mod = WarpLogic.properMod(pos.getX() + pos.getY() + pos.getZ(), values.size());
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

    record RandomHue(float saturation, float brightness) implements PortalColorApplier {
        public RandomHue(NbtCompound applierData) {
            this(InfinityOptions.test(applierData, "saturation", 1.0f),
            InfinityOptions.test(applierData, "brightness", 1.0f));
        }

        @Override
        public int apply(BlockPos pos) {
            return Color.HSBtoRGB((new Random(pos.hashCode())).nextFloat(), saturation, brightness);
        }
    }
}
