package net.lerariemann.infinity.util;

import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BackportMethods {
    public static int getOrDefaultInt(ItemStack stack, String key, int i) {
        if (stack.hasNbt()) {
            assert stack.getNbt() != null;
            return stack.getNbt().getInt(key);
        }
        return i;
    }

    public static ItemStack apply(ItemStack stack, String key, int i) {
        if (stack.hasNbt()) {
            assert stack.getNbt() != null;
            var c = new NbtCompound();
            c.putInt(key, i);
            stack.getNbt().putInt(key, i);
            return stack;
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
        if (stack.getNbt() != null) {
            return stack.getNbt().getString(ModItemFunctions.DESTINATION);
        }
        return null;
    }

    public static @Nullable Identifier getDimensionIdentifier(ItemStack stack) {
        String dimension = getDimensionComponents(stack);
        if (dimension != null) return Identifier.tryParse(dimension);
        else return null;
    }
}