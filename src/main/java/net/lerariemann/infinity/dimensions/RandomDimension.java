package net.lerariemann.infinity.dimensions;


import net.lerariemann.infinity.InfinityMod;
import net.minecraft.nbt.*;
import java.util.Random;


public class RandomDimension {
    private final NbtCompound data;
    private final int id;
    private final String PATH;
    private final RandomProvider PROVIDER;
    public String name;
    private final Random random;
    public int height;

    public RandomDimension(int i, RandomProvider provider, String path) {
        random = new Random(i);
        PROVIDER = provider;
        PATH = path;
        id = i;
        name = "generated_"+i;
        data = new NbtCompound();
        RandomDimensionType type = new RandomDimensionType(id, PROVIDER, PATH);
        data.put("type", NbtString.of(type.fullname));
        height = type.height;
        data.put("generator", randomDimensionGenerator());
        CommonIO.write(data, path + "/datapacks/" + InfinityMod.MOD_ID + "/data/" + InfinityMod.MOD_ID + "/dimension", name + ".json");
    }

    boolean weighedRandom(int weight0, int weight1) {
        int i = random.nextInt(weight0+weight1);
        return i < weight1;
    }

    NbtCompound randomDimensionGenerator() {
        NbtCompound res = new NbtCompound();
        WeighedStructure<Integer> list = new WeighedStructure<>();
        list.add(2, 0.0001);
        list.add(1, 0.01);
        list.add(0, 1.0);
        switch (list.getRandomElement(random)) {
            case 2 -> {
                res.put("type", NbtString.of("minecraft:debug"));
                return res;
            }
            case 1 -> {
                res.put("type", NbtString.of("minecraft:flat"));
                res.put("settings", randomSuperflatSettings());
                return res;
            }
            default -> {
                res.put("type", NbtString.of("minecraft:noise"));
                res.put("settings", NbtString.of(randomNoiseSettings()));
                res.put("biome_source", randomBiomeSource());
                return res;
            }
        }
    }

    NbtCompound superflatLayer(int height, WeighedStructure<String> str) {
        NbtCompound res = new NbtCompound();
        res.put("height", NbtInt.of(height));
        res.put("block", NbtString.of(str.getRandomElement(random)));
        return res;
    }

    NbtCompound randomSuperflatSettings() {
        NbtCompound res = new NbtCompound();
        NbtList layers = new NbtList();
        if (weighedRandom(1, 3)) {
            int layer_count = Math.min(64, 1 + (int) Math.floor(random.nextExponential() * 2));
            int heightLeft = height;
            for (int i = 0; i < layer_count; i++) {
                int layerHeight = Math.min(heightLeft, 1 + (int) Math.floor(random.nextExponential() * 2));
                heightLeft -= layerHeight;
                layers.add(superflatLayer(height, PROVIDER.FULL_BLOCKS));
                if (heightLeft <= 1) {
                    break;
                }
            }
            if (random.nextBoolean()) {
                layers.add(superflatLayer(1, PROVIDER.ALL_BLOCKS));
            }
        }
        res.put("layers", layers);
        res.put("biome", NbtString.of(randomBiome()));
        res.put("lakes", NbtByte.of(random.nextBoolean()));
        res.put("features", NbtByte.of(random.nextBoolean()));
        return res;
    }

    NbtCompound randomBiomeSource() {
        NbtCompound res = new NbtCompound();
        WeighedStructure<Integer> list = new WeighedStructure<>();
        list.add(3, 0.01);
        list.add(2, 0.495);
        list.add(1, 0.495);
        list.add(0, 1.0);
        switch (list.getRandomElement(random)) {
            case 3 -> {
                res.put("type", NbtString.of("minecraft:the_end"));
                return res;
            }
            case 2 -> {
                res.put("type", NbtString.of("minecraft:checkerboard"));
                res.put("biomes", randomBiomesCheckerboard());
                res.put("scale", NbtInt.of(Math.min(62, (int) Math.floor(random.nextExponential() * 2))));
                return res;
            }
            case 1 -> {
                res.put("type", NbtString.of("minecraft:multi_noise"));
                WeighedStructure<Integer> list2 = new WeighedStructure<>();
                list2.add(2, 0.1);
                list2.add(1, 0.1);
                list2.add(0, 0.8);
                switch (list2.getRandomElement(random)) {
                    case 2 -> res.put("preset", NbtString.of("minecraft:overworld"));
                    case 1 -> res.put("preset", NbtString.of("minecraft:nether"));
                    default -> res.put("biomes", randomBiomes());
                }
                return res;
            }
            default -> {
                res.put("type", NbtString.of("minecraft:fixed"));
                res.put("biome", NbtString.of(randomBiome()));
            }
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
            element.put("biome", NbtString.of(randomBiome()));
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
            double a = (random.nextDouble()-0.5)*4;
            double b = (random.nextDouble()-0.5)*4;
            res.put("min", NbtDouble.of(Math.min(a, b)));
            res.put("max", NbtDouble.of(Math.max(a, b)));
            return res;
        }
        return NbtDouble.of((random.nextDouble()-0.5)*4);
    }

    String randomBiome() {
        if (random.nextBoolean()) {
            return PROVIDER.BIOMES.getRandomElement(random);
        }
        else {
            RandomBiome biome = new RandomBiome(random.nextInt(), PROVIDER, PATH);
            return biome.fullname;
        }
    }

    String randomNoiseSettings() {
        if (random.nextBoolean()) {
            return PROVIDER.NOISE_PRESETS.getRandomElement(random);
        }
        else {
            RandomNoisePreset preset = new RandomNoisePreset(id, PROVIDER, PATH);
            return preset.fullname;
        }
    }
}
