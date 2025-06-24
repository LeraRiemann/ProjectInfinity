package net.lerariemann.infinity.options;

import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.function.Function;

import static net.lerariemann.infinity.util.core.NbtUtils.getFloat;

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
        return switch(NbtUtils.getString(comp, "type")) {
            case "constant" -> new Constant(getFloat(comp, "value", 1));
            case "add" -> new Add(getFloat(comp, "value", 1));
            default -> Empty.INSTANCE;
        };
    }
}
