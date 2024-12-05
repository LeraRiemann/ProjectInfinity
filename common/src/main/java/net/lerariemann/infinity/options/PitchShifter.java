package net.lerariemann.infinity.options;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.function.Function;

public interface PitchShifter {
    Function<Float, Float> applier();

    enum Empty implements PitchShifter {
        INSTANCE;
        @Override
        public Function<Float, Float> applier() {
            return Function.identity();
        }
    }
    record Constant(float value) implements PitchShifter {
        @Override
        public Function<Float, Float> applier() {
            return f -> value;
        }
    }
    record Add(float value) implements PitchShifter {
        @Override
        public Function<Float, Float> applier() {
            return f -> f + value;
        }
    }

    static PitchShifter decode(NbtCompound comp) {
        return switch(comp.getString("type")) {
            case "constant" -> new Constant(getFloat(comp, "value"));
            case "add" -> new Add(getFloat(comp, "value"));
            default -> Empty.INSTANCE;
        };
    }

    static float getFloat(NbtCompound comp, String s) {
        if (!comp.contains(s, NbtElement.FLOAT_TYPE)) return 1;
        return comp.getFloat(s);
    }
}
