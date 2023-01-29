package net.lerariemann.infinity.dimensions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtInt;

import java.io.IOException;
import java.util.Random;

public class RandomProvider {
    public WeighedStructure<String> FULL_BLOCKS;
    public WeighedStructure<String> ALL_BLOCKS;
    public WeighedStructure<String> BIOMES;
    public WeighedStructure<String> NOISE_PRESETS;
    public WeighedStructure<String> INFINIBURN;
    public WeighedStructure<String> PRECIPITATION;
    public WeighedStructure<String> SOUNDS;
    public WeighedStructure<String> MUSIC;
    public WeighedStructure<String> PARTICLES;
    public WeighedStructure<String> ITEMS;
    public WeighedStructure<String> MOBS;
    public WeighedStructure<String> MOBCATEGORIES;
    public String PATH;

    public RandomProvider(String path) throws IOException, CommandSyntaxException {
        PATH = path;
        ALL_BLOCKS = register("allblocks");
        FULL_BLOCKS = register("fullblocks");
        BIOMES = register("biomes");
        NOISE_PRESETS = register("noise_presets");
        INFINIBURN = register("infiniburn");
        PRECIPITATION = register("precipitation");
        SOUNDS = register("sounds");
        MUSIC = register("music");
        PARTICLES = register("particles");
        ITEMS = register("items");
        MOBS = register("mobs");
        MOBCATEGORIES = register("mobcategories");
    }

    WeighedStructure<String> register(String name) throws IOException, CommandSyntaxException {
        return CommonIO.commonListReader(PATH + name + ".json");
    }

    public static boolean weighedRandom(Random random, int weight0, int weight1) {
        int i = random.nextInt(weight0+weight1);
        return i < weight1;
    }
}
