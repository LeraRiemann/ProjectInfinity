package net.lerariemann.infinity.dimensions;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.*;

public class RandomProvider {
    public Map<String, WeighedStructure<String>> registry;
    public Map<String, WeighedStructure<NbtElement>> registry_uncommon;
    public Map<String, NbtList> presetregistry;
    public String configPath;

    public RandomProvider(String path) {
        configPath = path;
        registry = new HashMap<>();
        registry_uncommon = new HashMap<>();
        presetregistry = new HashMap<>();
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
        register("tree_decorators");
        register("trunk_placers");
        register("foliage_placers");
        register("multinoise_presets");
        for (String mob: mobcategories()) {
            mobregister(mob);
        }
        for (String preset: registry.get("multinoise_presets").keys) {
            if (!Objects.equals(preset, "none")) presetregister(preset);
        }
    }

    List<String> mobcategories() {
        return registry.get("mob_categories").keys;
    }

    void register(String key) {
        registry.put(key, CommonIO.commonListReader(configPath + "weighed_lists/" + key + ".json"));
    }

    void mobregister(String key) {
        registry.put(key, CommonIO.commonListReader(configPath + "mobs/" + key + ".json"));
    }

    void presetregister(String key) {
        presetregistry.put(key, CommonIO.read(configPath + "multinoisepresets/" + key + ".json").getList("elements", NbtElement.STRING_TYPE));
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

    static NbtCompound genBounds(Random random, int lbound, int bound) {
        NbtCompound value = new NbtCompound();
        int a = random.nextInt(lbound, bound);
        int b = random.nextInt(lbound, bound);
        value.putInt("min_inclusive", Math.min(a, b));
        value.putInt("max_inclusive", Math.max(a, b));
        return value;
    }

    public static NbtElement intProvider(Random random, int bound, boolean acceptDistributions) {
        return intProvider(random, 0, bound, acceptDistributions);
    }

    public static NbtElement intProvider(Random random, int lbound, int bound, boolean acceptDistributions) {
        int i = random.nextInt(acceptDistributions ? 6 : 4);
        NbtCompound res = new NbtCompound();
        switch(i) {
            case 0 -> {
                res.putString("type", "constant");
                res.putInt("value", random.nextInt(lbound, bound));
                return res;
            }
            case 1, 2 -> {
                res.putString("type", i==1 ? "uniform" : "biased_to_bottom");
                res.put("value", genBounds(random, lbound, bound));
                return res;
            }
            case 4 -> {
                res.putString("type", "clamped");
                NbtCompound value = genBounds(random, lbound, bound);
                value.put("source", intProvider(random, lbound, bound, false));
                res.put("value", value);
                return res;
            }
            case 3 -> {
                res.putString("type", "clamped_normal");
                NbtCompound value = genBounds(random, lbound, bound);
                value.putDouble("mean", lbound + random.nextDouble()*(bound-lbound));
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
                    element.put("data", intProvider(random, lbound, bound, false));
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
