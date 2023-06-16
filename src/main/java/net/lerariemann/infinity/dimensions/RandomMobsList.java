package net.lerariemann.infinity.dimensions;

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
        for (String i : PROVIDER.mobcategories()) {
            data.put(i, new NbtList());
        }
        int mobCount = random.nextInt(20);
        for(int i = 0; i < mobCount; i++) {
            String category = PROVIDER.randomName(random, "mob_categories");
            NbtCompound mob = new NbtCompound();
            String mobname = PROVIDER.randomName(random, category);
            mob.putString("type", mobname);
            biome.addMob(mobname);
            mob.putInt("weight", 1 + random.nextInt(20));
            int a = 1 + random.nextInt(6);
            int b = 1 + random.nextInt(6);
            mob.putInt("minCount", Math.min(a, b));
            mob.putInt("maxCount", Math.max(a, b));
            data.get(category).add(mob);
        }
    }

    NbtCompound asData() {
        NbtCompound res = new NbtCompound();
        for (String i : PROVIDER.mobcategories()) res.put(i, data.get(i));
        return res;
    }
}
