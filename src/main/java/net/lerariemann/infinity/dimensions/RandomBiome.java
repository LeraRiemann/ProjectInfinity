package net.lerariemann.infinity.dimensions;

import net.minecraft.nbt.NbtCompound;

import java.util.Random;

public class RandomBiome {
    private NbtCompound data;
    private int id;
    private RandomProvider PROVIDER;
    public String name;
    private Random random;

    RandomBiome(int i, RandomProvider provider) {
        random = new Random(i);
        PROVIDER = provider;
        id = i;
        name = "generated_"+i;
        data = new NbtCompound();
    }
}
