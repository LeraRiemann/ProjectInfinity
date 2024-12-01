package net.lerariemann.infinity.options;

import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.util.WarpLogic;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import static net.lerariemann.infinity.block.custom.IridescentBlock.num_models;

public interface IridescentMap {
    default int getColor(BlockPos pos) {
        return WarpLogic.properMod((int)(num_models * getHue(pos)), num_models);
    }
    default double getHue(BlockPos pos) {
        return Iridescence.sample(pos);
    }

    static IridescentMap decode(NbtCompound data) {
        if (!data.contains("type")) return Perliny.INSTANCE;
        return switch (data.getString("type")) {
            case "linear" -> Linear.INSTANCE;
            case "circles" -> new PrettyCircles(InfinityOptions.test(data, "scale", num_models / 2.0f));
            case "static" -> new Static(InfinityOptions.test(data, "value", 0));
            default -> Perliny.INSTANCE;
        };
    }

    enum Perliny implements IridescentMap {
        INSTANCE
    }
    enum Linear implements IridescentMap {
        INSTANCE;
        @Override
        public int getColor(BlockPos pos) {
            return WarpLogic.properMod(pos.getX() + pos.getY() + pos.getZ(), num_models);
        }
    }
    record PrettyCircles(double scale) implements IridescentMap {
        @Override
        public double getHue(BlockPos pos) {
            return (Math.cos(pos.getX()/scale) + Math.cos(pos.getY()/scale) + Math.cos(pos.getZ()/scale))/4;
        }
    }
    record Static(int value) implements IridescentMap {
        @Override
        public int getColor(BlockPos pos) {
            return WarpLogic.properMod(value, num_models);
        }
    }
}
