package net.lerariemann.infinity.dimensions;


import net.lerariemann.infinity.InfinityMod;
import net.minecraft.nbt.*;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Paths;
import java.util.*;


public class RandomDimension {
    public final int id;
    public final String storagePath;
    public final RandomProvider PROVIDER;
    public String name;
    public final Random random;
    public int height;
    public int min_y;
    public int sea_level;
    public List<String> vanilla_biomes;
    public List<Integer> random_biome_ids;
    public Map<String, String> top_blocks;

    public RandomDimension(int i, RandomProvider provider, String path) {
        random = new Random(i);
        PROVIDER = provider;
        id = i;
        name = "generated_"+i;
        String rootPath = path + "/" + name;
        storagePath = rootPath + "/data/" + InfinityMod.MOD_ID;
        NbtCompound data = new NbtCompound();
        vanilla_biomes = new ArrayList<>();
        random_biome_ids = new ArrayList<>();
        top_blocks = new HashMap<>();
        RandomDimensionType type = new RandomDimensionType(this);
        data.putString("type", type.fullname);
        data.put("generator", randomDimensionGenerator());
        for (Integer id: random_biome_ids) {
            RandomBiome b = new RandomBiome(id, this);
        }
        CommonIO.write(data, storagePath + "/dimension", name + ".json");
        if (!(Paths.get(rootPath + "/pack.mcmeta")).toFile().exists()) CommonIO.write(packMcmeta(), rootPath, "pack.mcmeta");
    }

    NbtCompound packMcmeta() {
        NbtCompound res = new NbtCompound();
        NbtCompound pack = new NbtCompound();
        pack.putInt("pack_format", 10);
        pack.putString("description", "Dimension #" + id);
        res.put("pack", pack);
        return res;
    }

    NbtCompound randomDimensionGenerator() {
        NbtCompound res = new NbtCompound();
        String type = PROVIDER.randomName(random, "generator_types");
        res.putString("type", type);
        switch (type) {
            case "minecraft:flat" -> {
                res.put("settings", randomSuperflatSettings());
                return res;
            }
            case "minecraft:noise" -> {
                res.put("biome_source", randomBiomeSource());
                res.putString("settings", randomNoiseSettings());
                return res;
            }
            default -> {
                return res;
            }
        }
    }

    NbtCompound superflatLayer(int h, String block) {
        NbtCompound res = new NbtCompound();
        res.putInt("height", h);
        res.putString("block", block);
        return res;
    }

    NbtCompound randomSuperflatSettings() {
        NbtCompound res = new NbtCompound();
        NbtList layers = new NbtList();
        String biome = randomBiome();
        if (RandomProvider.weighedRandom(random, 1, 3)) {
            int layer_count = Math.min(64, 1 + (int) Math.floor(random.nextExponential() * 2));
            int heightLeft = height;
            for (int i = 0; i < layer_count; i++) {
                int layerHeight = Math.min(heightLeft, 1 + (int) Math.floor(random.nextExponential() * 2));
                heightLeft -= layerHeight;
                layers.add(superflatLayer(layerHeight, PROVIDER.randomName(random, "full_blocks_worldgen")));
                if (heightLeft <= 1) {
                    break;
                }
            }
            if (random.nextBoolean()) {
                String block = PROVIDER.randomName(random, "top_blocks");
                top_blocks.put(biome, block);
                layers.add(superflatLayer(1, block));
            }
        }
        res.putString("biome", biome);
        res.put("layers", layers);
        res.putBoolean("lakes", random.nextBoolean());
        res.putBoolean("features", random.nextBoolean());
        return res;
    }

    NbtCompound randomBiomeSource() {
        NbtCompound res = new NbtCompound();
        String type = PROVIDER.randomName(random, "biome_source_types");
        res.putString("type",type);
        switch (type) {
            case "minecraft:the_end" -> {
                return res;
            }
            case "minecraft:checkerboard" -> {
                res.put("biomes", randomBiomesCheckerboard());
                res.putInt("scale", Math.min(62, (int) Math.floor(random.nextExponential() * 2)));
                return res;
            }
            case "minecraft:multi_noise" -> {
                WeighedStructure<Integer> list2 = new WeighedStructure<>();
                list2.add(2, 0.1);
                list2.add(1, 0.1);
                list2.add(0, 0.8);
                switch (list2.getRandomElement(random)) {
                    case 2 -> res.putString("preset", "minecraft:overworld");
                    case 1 -> res.putString("preset", "minecraft:nether");
                    default -> res.put("biomes", randomBiomes());
                }
                return res;
            }
            case "minecraft:fixed" -> res.putString("biome", randomBiome());
        }
        return res;
    }

    NbtList randomBiomesCheckerboard() {
        NbtList res = new NbtList();
        int biome_count = Math.min(64, 2 + (int) Math.floor(random.nextExponential()));
        for (int i = 0; i < biome_count; i++) {
            res.add(NbtString.of(randomBiome()));
        }
        return res;
    }

    NbtList randomBiomes() {
        NbtList res = new NbtList();
        int biome_count = Math.min(16, 2 + (int) Math.floor(random.nextExponential()));
        for (int i = 0; i < biome_count; i++) {
            NbtCompound element = new NbtCompound();
            element.putString("biome", randomBiome());
            element.put("parameters", randomMultiNoiseParameters());
            res.add(element);
        }
        return res;
    }

    NbtCompound randomMultiNoiseParameters() {
        NbtCompound res = new NbtCompound();
        res.put("temperature", randomMultiNoiseParameter());
        res.put("humidity", randomMultiNoiseParameter());
        res.put("continentalness", randomMultiNoiseParameter());
        res.put("erosion", randomMultiNoiseParameter());
        res.put("weirdness", randomMultiNoiseParameter());
        res.put("depth", randomMultiNoiseParameter());
        res.put("offset", NbtDouble.of(random.nextDouble()));
        return res;
    }

    NbtElement randomMultiNoiseParameter() {
        if (random.nextBoolean()) {
            NbtCompound res = new NbtCompound();
            double a = (random.nextFloat()-0.5)*4;
            double b = (random.nextFloat()-0.5)*4;
            res.putFloat("min", (float)Math.min(a, b));
            res.putFloat("max", (float)Math.max(a, b));
            return res;
        }
        return NbtDouble.of((random.nextDouble()-0.5)*4);
    }

    String randomBiome() {
        String biome;
        if (RandomProvider.weighedRandom(random, 3, 1)) {
            biome = PROVIDER.randomName(random, "biomes");
            vanilla_biomes.add(biome);
        }
        else {
            int id = random.nextInt();
            random_biome_ids.add(id);
            LogManager.getLogger().info(id);
            biome = "infinity:biome_" + id;
        }
        return biome;
    }

    String randomNoiseSettings() {
        if (RandomProvider.weighedRandom(random, 3, 1)) {
            String result = PROVIDER.randomName(random, "noise_presets");
            String default_block;
            switch(result) {
                case "minecraft:end" -> default_block = "minecraft:endstone";
                case "minecraft:nether" -> default_block = "minecraft:netherrack";
                default -> default_block = "minecraft:grass_block";
            }
            for (int id: random_biome_ids) {
                top_blocks.put("infinity:biome_"+id, default_block);
            }
            return result;
        }
        else {
            RandomNoisePreset preset = new RandomNoisePreset(this);
            return preset.fullname;
        }
    }
}
