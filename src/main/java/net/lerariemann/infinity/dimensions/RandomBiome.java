package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.nbt.NbtCompound;

import java.util.Random;

public class RandomBiome {
    private NbtCompound data;
    private int id;
    private RandomProvider PROVIDER;
    public String name;
    public String fullname;
    private Random random;

    RandomBiome(int i, RandomProvider provider, String path) {
        random = new Random(i);
        PROVIDER = provider;
        id = i;
        name = "generated_" +i;
        fullname = InfinityMod.MOD_ID + ":" + name;
        data = new NbtCompound();

    }
}
