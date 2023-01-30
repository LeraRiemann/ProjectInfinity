package net.lerariemann.infinity.dimensions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.io.IOException;
import java.util.Random;

public class RandomProvider {
    public WeighedStructure<String> FULL_BLOCKS;
    public WeighedStructure<String> ALL_BLOCKS;
    public WeighedStructure<String> BIOMES;
    public WeighedStructure<String> NOISE_PRESETS;
    public WeighedStructure<String> TAGS;
    public WeighedStructure<String> PRECIPITATION;
    public WeighedStructure<String> SOUNDS;
    public WeighedStructure<String> MUSIC;
    public WeighedStructure<String> PARTICLES;
    public WeighedStructure<String> ITEMS;
    public WeighedStructure<String> MOBS;
    public WeighedStructure<String> MOB_CATEGORIES;
    public WeighedStructure<String> FLUIDS;
    public WeighedStructure<String> AIR;
    public WeighedStructure<String> BIOME_SOURCES;
    public WeighedStructure<String> GENERATOR_TYPES;
    public String PATH;

    public RandomProvider(String path) throws IOException, CommandSyntaxException {
        PATH = path;
        ALL_BLOCKS = register("allblocks");
        FULL_BLOCKS = register("fullblocks");
        BIOMES = register("biomes");
        NOISE_PRESETS = register("noise_presets");
        TAGS = register("tags");
        PRECIPITATION = register("precipitation");
        SOUNDS = register("sounds");
        MUSIC = register("music");
        PARTICLES = register("particles");
        ITEMS = register("items");
        MOBS = register("mobs");
        MOB_CATEGORIES = register("mobcategories");
        FLUIDS = register("fluids");
        AIR = register("airs");
        BIOME_SOURCES = register("biomesourcetype");
        GENERATOR_TYPES = register("generatortype");
    }

    WeighedStructure<String> register(String name) throws IOException, CommandSyntaxException {
        return CommonIO.commonListReader(PATH + name + ".json");
    }

    public static boolean weighedRandom(Random random, int weight0, int weight1) {
        int i = random.nextInt(weight0+weight1);
        return i < weight1;
    }

    public static NbtCompound Block(String block) {
        NbtCompound res = new NbtCompound();
        res.putString("Name", block);
        if (block.contains("_leaves")) {
            NbtCompound properties = new NbtCompound();
            properties.putBoolean("persistent", true);
            res.put("Properties", properties);
        }
        return res;
    }

    public static NbtCompound blockToProvider(NbtCompound block) {
        NbtCompound res = new NbtCompound();
        res.putString("type", "minecraft:simple_state_provider");
        res.put("state", block);
        return res;
    }

    public String randomName(Random random, WeighedStructure<String> STR) {
        return STR.getRandomElement(random);
    }

    public NbtCompound randomBlock(Random random, WeighedStructure<String> STR) {
        return Block(randomName(random, STR));
    }

    public NbtCompound randomBlockProvider (Random random, WeighedStructure<String> STR) {
        return blockToProvider(randomBlock(random, STR));
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
