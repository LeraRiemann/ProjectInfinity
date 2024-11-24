package net.lerariemann.infinity.options;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.function.Function;

public class PitchShifter {
    Type type;
    float value;

    String getString(NbtCompound comp, String s) {
        if (!comp.contains(s, NbtElement.STRING_TYPE)) return "empty";
        return comp.getString(s);
    }

    float getFloat(NbtCompound comp, String s) {
        if (!comp.contains(s, NbtElement.FLOAT_TYPE)) return 1;
        return comp.getFloat(s);
    }

    PitchShifter(NbtCompound comp) {
        type = decodeType(getString(comp, "type"));
        if (type != Type.EMPTY) {
            value = getFloat(comp, "value");
        }
    }

    PitchShifter() {
        type = Type.EMPTY;
    }

    Function<Float, Float> applier() {
        return switch (type) {
            case EMPTY -> f -> f;
            case CONSTANT -> f -> value;
            case ADD -> f -> f + value;
        };
    }

    Type decodeType(String s) {
        return switch (s) {
            case "constant" -> Type.CONSTANT;
            case "add" -> Type.ADD;
            default -> Type.EMPTY;
        };
    }

    enum Type {
        EMPTY,
        CONSTANT,
        ADD
    }
}
