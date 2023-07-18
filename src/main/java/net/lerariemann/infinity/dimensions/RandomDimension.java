package net.lerariemann.infinity.dimensions;


import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
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
    public NbtCompound default_block;
    public NbtCompound default_fluid;
    public List<NbtCompound> additional_blocks;
    public List<String> vanilla_biomes;
    public List<Integer> random_biome_ids;
    public Map<String, NbtCompound> top_blocks;
    public Map<String, NbtCompound> underwater;
    public String type_alike;
    public MinecraftServer server;

    public RandomDimension(int i, MinecraftServer server) {
        random = new Random(i);
        this.server = server;
        PROVIDER = ((MinecraftServerAccess)(server)).getDimensionProvider();
        id = i;
        name = "generated_"+i;
        String rootPath = server.getSavePath(WorldSavePath.DATAPACKS).toString() + "/" + name;
        storagePath = rootPath + "/data/" + InfinityMod.MOD_ID;
        createDirectories();
        NbtCompound data = new NbtCompound();
        vanilla_biomes = new ArrayList<>();
        random_biome_ids = new ArrayList<>();
        top_blocks = new HashMap<>();
        underwater = new HashMap<>();
        default_block = RandomProvider.Block("minecraft:stone");
        default_fluid = RandomProvider.Block("minecraft:water");
        additional_blocks = new ArrayList<>();
        type_alike = PROVIDER.randomName(random, "noise_presets");
        RandomDimensionType type = new RandomDimensionType(this);
        data.putString("type", type.fullname);
        data.put("generator", randomDimensionGenerator());
        for (Integer id: random_biome_ids) if (does_not_contain(RegistryKeys.BIOME, "biome_"+id)) {
            RandomBiome b = new RandomBiome(id, this);
        }
        CommonIO.write(data, storagePath + "/dimension", name + ".json");
        if (!(Paths.get(rootPath + "/pack.mcmeta")).toFile().exists()) CommonIO.write(packMcmeta(), rootPath, "pack.mcmeta");
    }

    public <T> boolean does_not_contain(RegistryKey<? extends Registry<T>> key, String name) {
        return !(server.getRegistryManager().get(key).contains(RegistryKey.of(key, new Identifier(InfinityMod.MOD_ID, name))));
    }

    void createDirectories() {
        for (String s: new String[]{"dimension", "dimension_type", "worldgen/biome", "worldgen/configured_feature",
                "worldgen/placed_feature", "worldgen/noise_settings", "worldgen/configured_carver"}) {
            try {
                Files.createDirectories(Paths.get(storagePath + "/" + s));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    boolean isNotOverworld() {
        return (!Objects.equals(type_alike, "minecraft:overworld")) && (!Objects.equals(type_alike, "minecraft:large_biomes"))
                && (!Objects.equals(type_alike, "minecraft:amplified")) && (!Objects.equals(type_alike, "infinity:whack"));
    }

    boolean hasCeiling() {
        return ((Objects.equals(type_alike, "minecraft:nether")) || (Objects.equals(type_alike, "minecraft:caves")));
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
        String block = "minecraft:air";
        int layer_count = Math.min(64, 1 + (int) Math.floor(random.nextExponential() * 2));
        int heightLeft = height;
        for (int i = 0; i < layer_count; i++) {
            int layerHeight = Math.min(heightLeft, 1 + (int) Math.floor(random.nextExponential() * 4));
            heightLeft -= layerHeight;
            block = PROVIDER.randomName(random, "full_blocks_worldgen");
            layers.add(superflatLayer(layerHeight, block));
            if (heightLeft <= 1) {
                break;
            }
        }
        if (random.nextBoolean()) {
            block = PROVIDER.randomName(random, "top_blocks");
            layers.add(superflatLayer(1, block));
        }
        res.putString("biome", biome);
        res.put("layers", layers);
        res.putBoolean("lakes", random.nextBoolean());
        res.putBoolean("features", random.nextBoolean());
        top_blocks.put(biome, RandomProvider.Block(block));
        underwater.put(biome, RandomProvider.Block(block));
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
                String preset = PROVIDER.randomName(random, "multinoise_presets");
                if (Objects.equals(preset, "none")) res.put("biomes", randomBiomes());
                else {
                    res.putString("preset", preset);
                    addPresetBiomes(preset);
                }
                return res;
            }
            case "minecraft:fixed" -> res.putString("biome", randomBiome());
        }
        return res;
    }

    void addPresetBiomes(String preset) {
        NbtList lst = PROVIDER.biomePresetRegistry.get(preset);
        for (NbtElement i: lst) {
            vanilla_biomes.add(i.asString());
        }
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
        if (!PROVIDER.roll(random, "use_random_biome")) {
            biome = PROVIDER.randomName(random, "biomes");
            vanilla_biomes.add(biome);
        }
        else {
            int id = random.nextInt();
            random_biome_ids.add(id);
            biome = "infinity:biome_" + id;
        }
        return biome;
    }

    String randomNoiseSettings() {
        RandomNoisePreset preset = new RandomNoisePreset(this);
        return preset.fullname;
    }
}