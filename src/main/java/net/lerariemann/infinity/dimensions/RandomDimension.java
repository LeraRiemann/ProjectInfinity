package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.nbt.*;

import java.util.Random;

import static java.lang.Math.min;

public class RandomDimension {
    private NbtCompound data;
    private int id;
    private RandomProvider PROVIDER;
    public String name;
    private Random random;
    public int height;

    public RandomDimension(int i, RandomProvider provider) {
        random = new Random(i);
        PROVIDER = provider;
        id = i;
        name = "generated_"+i;
        height = 256;
        data = new NbtCompound();
        data.put("type", randomDimensionType());
        data.put("generator", randomDimensionGenerator());
        CommonIO.write(data, "config/output.json");
    }

    NbtCompound getData() {return data;}

    boolean weighedRandom(int weight0, int weight1) {
        int i = random.nextInt(weight0+weight1);
        return i < weight1;
    }

    double coordinateScale() {
        WeighedStructure<Double> values = new WeighedStructure<>();
        values.add(1.0, 2.0);
        values.add(8.0, 2.0);
        double random1 = Math.min(random.nextExponential(), 16.0);
        values.add(Math.exp(random1+3.0), 1.0);
        values.add(Math.exp(-random1), 1.0);
        values.add(1.0 + 7*random.nextDouble(), 2.0);
        return values.getRandomElement(random);
    }

    double ambientLight() {
        if (random.nextBoolean())
            return 0.0;
        return random.nextDouble();
    }

    int minY() {
        int random1 = Math.min(126, (int)Math.floor(random.nextExponential()*2));
        return -16*random1;
    }

    NbtCompound randomDimensionType(){
        NbtCompound res = new NbtCompound();
        res.put("ultrawarm", NbtByte.of(random.nextBoolean()));
        res.put("natural", NbtByte.of(random.nextBoolean()));
        res.put("has_skylight", NbtByte.of(random.nextBoolean()));
        res.put("piglin_safe", NbtByte.of(random.nextBoolean()));
        res.put("bed_works", NbtByte.of(random.nextBoolean()));
        res.put("respawn_anchor_works", NbtByte.of(random.nextBoolean()));
        res.put("has_raids", NbtByte.of(random.nextBoolean()));
        res.put("coordinate_scale", NbtDouble.of(coordinateScale()));
        res.put("ambient_light", NbtDouble.of(ambientLight()));
        if (random.nextBoolean()){
            res.put("fixed_time", NbtInt.of(random.nextInt(24000)));
        }
        int min_y = minY();
        res.put("min_y", NbtInt.of(min_y));
        int max_y = -minY();
        height = max_y - min_y;
        res.put("height", NbtInt.of(max_y - min_y));
        return res;
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
            NbtList res = new NbtList();
            res.add(NbtDouble.of((random.nextDouble()-0.5)*4));
            res.add(NbtDouble.of((random.nextDouble()-0.5)*4));
            return res;
        }
        return NbtDouble.of((random.nextDouble()-0.5)*4);
    }

    String randomBiome() {
        if (true) {
            return PROVIDER.BIOMES.getRandomElement(random);
        }
        else {
            RandomBiome biome = new RandomBiome(random.nextInt(), PROVIDER);
            return biome.name;
        }
    }

    String randomNoiseSettings() {
        if (true) {
            return PROVIDER.NOISE_PRESETS.getRandomElement(random);
        }
        else {
            RandomNoisePreset preset = new RandomNoisePreset(id, PROVIDER);
            return preset.name;
        }
    }
}
