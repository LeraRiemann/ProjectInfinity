package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.util.core.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandomMobsList {
    public RandomBiome parent;
    public Random random;
    public final RandomProvider PROVIDER;
    public Map<String, NbtList> data;

    RandomMobsList(RandomBiome biome) {
        parent = biome;
        random = biome.random;
        PROVIDER = biome.PROVIDER;
        data = new HashMap<>();
        for (String i : PROVIDER.mob_categories()) {
            data.put(i, new NbtList());
        }
        int mobCount = random.nextInt(20);
        for(int i = 0; i < mobCount; i++) {
            String category = PROVIDER.randomName(random, "mob_categories");
            NbtCompound mob = biome.addMob(category, true);
            data.get(category).add(mob);
        }
    }

    NbtCompound asData() {
        NbtCompound res = new NbtCompound();
        for (String i : PROVIDER.mob_categories()) res.put(i, data.get(i));
        return res;
    }
}
