package net.lerariemann.infinity.util;

import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BackportMethods {
    public static int getOrDefaultInt(ItemStack stack, String key, int i) {
        if (stack.hasNbt()) {
            assert stack.getNbt() != null;
            return stack.getNbt().getInt(key);
        }
        return i;
    }

    public static String getOrDefaultString(ItemStack stack, String key, String i) {
        if (stack.hasNbt()) {
            assert stack.getNbt() != null;
            return stack.getNbt().getString(key);
        }
        return i;
    }

    public static boolean contains(ItemStack stack, String key) {
        if (stack.hasNbt()) {
            return !Objects.requireNonNull(stack.getSubNbt(key)).isEmpty();
        }
        return false;
    }

    public static ItemStack apply(ItemStack stack, String key, int i) {
        if (stack.hasNbt()) {
            assert stack.getNbt() != null;
            stack.getNbt().putInt(key, i);
            return stack;
        } else {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putInt(key, i);
            stack.setNbt(nbtCompound);
        }
        return stack;
    }

    public static ItemStack apply(ItemStack stack, String key, String i) {
        if (stack.hasNbt()) {
            assert stack.getNbt() != null;
            stack.getNbt().putString(key, i);
            return stack;
        } else {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putString(key, i);
            stack.setNbt(nbtCompound);
        }
        return stack;
    }

    public static @Nullable String getBiomeComponents(ItemStack stack) {
        if (stack.getNbt() != null) {
            return stack.getNbt().getCompound("BlockEntityTag").getString("Biome");
        }
        return null;
    }

    public static @Nullable String getDimensionComponents(ItemStack stack) {
        if (stack.getNbt() != null && stack.getNbt().contains(ModComponentTypes.DESTINATION)) {
            return stack.getNbt().getString(ModComponentTypes.DESTINATION);
        }
        return null;
    }

    public static @Nullable Identifier getDimensionIdentifier(ItemStack stack) {
        String dimension = getDimensionComponents(stack);
        if (dimension != null) return Identifier.tryParse(dimension);
        else return null;
    }
}
