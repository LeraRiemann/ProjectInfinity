package net.lerariemann.infinity.dimensions;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandomProvider {
    public Map<String, WeighedStructure<String>> registry;
    public Map<String, WeighedStructure<NbtElement>> registry_uncommon;
    public String configPath;

    public RandomProvider(String path) {
        configPath = path;
        registry = new HashMap<>();
        registry_uncommon = new HashMap<>();
        register_uncommon("all_blocks");
        register_uncommon("top_blocks");
        register_uncommon("blocks_features");
        register_uncommon("full_blocks");
        register_uncommon("full_blocks_worldgen");
        register("biomes");
        register("noise_presets");
        register("tags");
        register("precipitation");
        register("sounds");
        register("music");
        register("particles");
        register("items");
        register("mobs");
        register("mob_categories");
        register("fluids");
        register("airs");
        register("biome_source_types");
        register("generator_types");
    }

    void register(String key) {
        registry.put(key, CommonIO.commonListReader(configPath + "weighed_lists/" + key + ".json"));
    }
    void register_uncommon(String key) {
        registry_uncommon.put(key, CommonIO.uncommonListReader(configPath + "weighed_lists/" + key + ".json"));
    }

    public static boolean weighedRandom(Random random, int weight0, int weight1) {
        int i = random.nextInt(weight0+weight1);
        return i < weight1;
    }

    public static NbtCompound Block(String block) {
        NbtCompound res = new NbtCompound();
        res.putString("Name", block);
        return res;
    }

    public static NbtCompound blockToProvider(NbtCompound block) {
        NbtCompound res = new NbtCompound();
        res.putString("type", "minecraft:simple_state_provider");
        res.put("state", block);
        return res;
    }

    public String randomName(Random random, String key) {
        switch (key) {
            case "all_blocks", "top_blocks", "blocks_features", "full_blocks", "full_blocks_worldgen" -> {
                NbtElement compound = registry_uncommon.get(key).getRandomElement(random);
                if (compound instanceof NbtCompound) return ((NbtCompound)compound).getString("Name");
                else return compound.asString();
            }
            default -> {
                return registry.get(key).getRandomElement(random);
            }
        }
    }

    public NbtCompound randomBlock(Random random, String key) {
        switch (key) {
            case "all_blocks", "top_blocks", "blocks_features", "full_blocks", "full_blocks_worldgen" -> {
                NbtElement compound = registry_uncommon.get(key).getRandomElement(random);
                if (compound instanceof NbtCompound) return ((NbtCompound)compound);
                else return Block(compound.asString());
            }
            default -> {
                return Block(registry.get(key).getRandomElement(random));
            }
        }
    }

    public NbtCompound randomBlockProvider (Random random, String key) {
        return blockToProvider(randomBlock(random, key));
    }

    static NbtCompound genBounds(Random random, int bound) {
        NbtCompound value = new NbtCompound();
        int a = random.nextInt(bound);
        int b = random.nextInt(bound);
        value.putInt("min_inclusive", Math.min(a, b));
        value.putInt("max_inclusive", Math.max(a, b));
        return value;
    }

    public static NbtElement intProvider(Random random, int bound, boolean acceptDistributions) {
        int i = random.nextInt(acceptDistributions ? 6 : 4);
        NbtCompound res = new NbtCompound();
        switch(i) {
            case 0 -> {
                res.putString("type", "constant");
                res.putInt("value", random.nextInt(bound));
                return res;
            }
            case 1, 2 -> {
                res.putString("type", i==1 ? "uniform" : "biased_to_bottom");
                res.put("value", genBounds(random, bound));
                return res;
            }
            case 4 -> {
                res.putString("type", "clamped");
                NbtCompound value = genBounds(random, bound);
                value.put("source", intProvider(random, bound, false));
                res.put("value", value);
                return res;
            }
            case 3 -> {
                res.putString("type", "clamped_normal");
                NbtCompound value = genBounds(random, bound);
                value.putDouble("mean", random.nextDouble()*bound);
                value.putDouble("deviation", random.nextExponential());
                res.put("value", value);
                return res;
            }
            case 5 -> {
                res.putString("type", "weighted_list");
                int j = 2 + random.nextInt(0, 5);
                NbtList list = new NbtList();
                for (int k=0; k<j; k++) {
                    NbtCompound element = new NbtCompound();
                    element.put("data", intProvider(random, bound, false));
                    element.putInt("weight", random.nextInt(100));
                    list.add(element);
                }
                res.put("distribution", list);
                return res;
            }
        }
        return res;
    }
}
