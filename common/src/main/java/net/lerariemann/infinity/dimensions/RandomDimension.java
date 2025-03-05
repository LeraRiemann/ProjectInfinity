package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.options.RandomInfinityOptions;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.lerariemann.infinity.util.core.RandomProvider;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.biome.Biome;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class RandomDimension {
    public final long numericId;
    public final RandomProvider PROVIDER;
    public Identifier identifier;
    public final Random random;
    public int height;
    public int min_y;
    public int sea_level;
    public boolean randomiseblocks;
    public NbtCompound default_block;
    public NbtCompound deepslate;
    public NbtCompound default_fluid;
    public List<NbtCompound> additional_blocks;
    public List<String> vanilla_biomes;
    public List<Long> random_biome_ids;
    public List<RandomBiome> random_biomes;
    public Map<String, NbtCompound> top_blocks;
    public Map<String, List<String>> structure_ids;
    public Map<String, NbtCompound> underwater;
    public String type_alike;
    public MinecraftServer server;
    public NbtCompound data;
    public RandomDimensionType type;

    public RandomDimension(Identifier id, MinecraftServer server) {
        this.server = server;
        PROVIDER = InfinityMod.provider;
        identifier = id;
        numericId = InfinityMethods.getNumericFromId(identifier);
        random = new Random(numericId);
        initializeStorage();
        /* Code for easter dimensions */
        if (PROVIDER.easterizer.easterize(this)) {
            wrap_up(true);
            return;
        }
        /* Code for procedurally generated dimensions */
        genBasics();
        type = new RandomDimensionType(this);
        data.putString("type", type.fullname);
        data.put("generator", randomDimensionGenerator());
        for (Long l: random_biome_ids) if (doesNotContain(RegistryKeys.BIOME, "biome_"+l)) {
            RandomBiome b = new RandomBiome(l, this);
            random_biomes.add(b);
            addStructures(b);
        }
        writeTags(getRootPath());
        wrap_up(false);
    }

    public String getName() {
        return identifier.getPath();
    }

    public String getRootPath() {
        return server.getSavePath(WorldSavePath.DATAPACKS).resolve(getName()).toString();
    }

    public String getStoragePath() {
        return server.getSavePath(WorldSavePath.DATAPACKS).resolve(getName()).resolve("data").resolve(InfinityMod.MOD_ID).toString();
    }

    public void initializeStorage() {
        data = new NbtCompound();
        vanilla_biomes = new ArrayList<>();
        random_biome_ids = new ArrayList<>();
        random_biomes = new ArrayList<>();
        top_blocks = new HashMap<>();
        underwater = new HashMap<>();
        structure_ids = new HashMap<>();
        additional_blocks = new ArrayList<>();
    }

    public void genBasics() {
        type_alike = PROVIDER.randomName(random, "noise_presets");
        min_y = 16*Math.clamp((int)Math.floor(random.nextExponential() * 2), isOverworldLike() ? -125 : -3, 0);
        int avgHeight = Math.clamp(RandomProvider.ruleInt("avgDimensionHeight"), 64, 1024);
        int max_y = 16*Math.clamp((int)Math.floor(random.nextGaussian(avgHeight/16.0, avgHeight/64.0)), isOverworldLike() ? 1 : 5, 125);
        randomiseblocks = PROVIDER.roll(random, "randomise_blocks");
        int sea_level_default = 63;
        if (!isOverworldLike()) sea_level_default = switch(type_alike) {
            case "minecraft:floating_islands" -> -64;
            case "minecraft:end" -> 0;
            case "minecraft:nether", "minecraft:caves" -> 32;
            default -> 63;
        };
        sea_level = randomiseblocks ? (int)Math.floor(random.nextGaussian(sea_level_default, 8)) : sea_level_default;
        max_y = Math.max(max_y, 16 * (int) (1 + Math.floor(sea_level / 16.0)));
        height = max_y - min_y;
        default_block = randomiseblocks ?
                PROVIDER.randomElement(random, "full_blocks_worldgen") :
                NbtUtils.nameToElement(getDefaultBlock("minecraft:stone"));
        default_fluid = randomiseblocks ?
                PROVIDER.randomElement(random, "fluids") :
                NbtUtils.nameToFluid(getDefaultFluid());
        deepslate = Arrays.stream((new String[]{"minecraft:overworld", "minecraft:amplified", "infinity:whack"})).toList().contains(type_alike) ?
                NbtUtils.nameToElement("minecraft:deepslate") : default_block;
    }

    void wrap_up(boolean isEasterDim) {
        if (!isEasterDim) (new DimensionData(this)).save();
        (new RandomInfinityOptions(this, isEasterDim)).save();
        CommonIO.write(data, getStoragePath() + "/dimension", getName() + ".json");
        if (!(Paths.get(getRootPath() + "/pack.mcmeta")).toFile().exists()) CommonIO.write(packMcmeta(), getRootPath(), "pack.mcmeta");
    }

    String getDefaultBlock(String fallback) {
        switch(type_alike) {
            case "minecraft:end" -> {
                return "minecraft:end_stone";
            }
            case "minecraft:nether" -> {
                return "minecraft:netherrack";
            }
            default -> {
                return fallback;
            }
        }
    }
    String getDefaultFluid() {
        switch(type_alike) {
            case "minecraft:end" -> {
                return "minecraft:air";
            }
            case "minecraft:nether" -> {
                return "minecraft:lava";
            }
            default -> {
                return "minecraft:water";
            }
        }
    }

    public <T> boolean doesNotContain(RegistryKey<? extends Registry<T>> key, String name) {
        return !(server.getRegistryManager().get(key).contains(RegistryKey.of(key, InfinityMethods.getId(name))));
    }

    boolean isOverworldLike() {
        return (type_alike.equals("minecraft:overworld")) || (type_alike.equals("minecraft:large_biomes"))
                || (type_alike.equals("minecraft:amplified")) || (type_alike.equals("infinity:whack"));
    }

    boolean hasCeiling() {
        return ((type_alike.equals("minecraft:nether")) || (type_alike.equals("minecraft:caves")) || (type_alike.equals("infinity:tangled")));
    }

    NbtCompound packMcmeta() {
        NbtCompound res = new NbtCompound();
        NbtCompound pack = new NbtCompound();
        pack.putInt("pack_format", 34);
        pack.putString("description", "Dimension #" + numericId);
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
                res.putLong("seed", numericId ^ server.getOverworld().getSeed());
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
        top_blocks.put(biome, NbtUtils.nameToElement(block));
        underwater.put(biome, NbtUtils.nameToElement(block));
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
                if (preset.equals("none") || hasCeiling()) res.put("biomes", randomBiomes());
                else {
                    res.putString("preset", preset.replace("_", ":"));
                    addPresetBiomes(preset);
                }
                return res;
            }
            case "minecraft:fixed" -> res.putString("biome", randomBiome());
        }
        return res;
    }

    void addPresetBiomes(String preset) {
        TagKey<Biome> tag = preset.equals("overworld") ? BiomeTags.IS_OVERWORLD : BiomeTags.IS_NETHER;
        Registry<Biome> r = server.getRegistryManager().get(RegistryKeys.BIOME);
        r.getKeys().forEach(key -> {
            if (!Objects.equals(key.getValue().getNamespace(), "infinity")) {
                if (r.get(key) != null && r.getEntry(r.get(key)).isIn(tag)) vanilla_biomes.add(key.getValue().toString());
            }
        });
    }

    int getBiomeCount() {
        return random.nextInt(2, Math.clamp(RandomProvider.ruleInt("maxBiomeCount"), 2, 10));
    }

    NbtList randomBiomesCheckerboard() {
        NbtList res = new NbtList();
        int biome_count = getBiomeCount();
        for (int i = 0; i < biome_count; i++) {
            res.add(NbtString.of(randomBiome()));
        }
        return res;
    }
    NbtList randomBiomes() {
        NbtList res = new NbtList();
        int biome_count = getBiomeCount();
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
            double a = (random.nextFloat()-0.5)*2;
            double b = (random.nextFloat()-0.5)*2;
            res.putFloat("min", (float)Math.min(a, b));
            res.putFloat("max", (float)Math.max(a, b));
            return res;
        }
        return NbtDouble.of((random.nextDouble()-0.5)*2);
    }

    String randomBiome() {
        String biome;
        if (!hasCeiling() && !PROVIDER.roll(random, "use_random_biome")) {
            biome = PROVIDER.randomName(random, "biomes");
            vanilla_biomes.add(biome);
        }
        else {
            long id = InfinityMethods.getRandomSeed(random);
            random_biome_ids.add(id);
            biome = "infinity:biome_" + id;
        }
        return biome;
    }

    String randomNoiseSettings() {
        RandomNoisePreset preset = new RandomNoisePreset(this);
        return preset.fullname;
    }

    void addStructures(RandomBiome b) {
        int numstructures = random.nextInt(1, 5);
        Set<String> temp = new HashSet<>();
        for (int i = 0; i < numstructures; i++) {
            addStructure(new RandomStructure(random.nextInt(), b), temp);
        }
        if (PROVIDER.roll( random, "text")) {
            addStructure(new RandomText(random.nextInt(), b), temp);
        }
    }
    void addStructure(RandomStructure s, Set<String> temp) {
        if (!temp.contains(s.name)) {
            temp.add(s.name);
            s.save();
            if (!structure_ids.containsKey(s.type)) structure_ids.put(s.type, new ArrayList<>());
            structure_ids.get(s.type).add(s.fullname);
        }
    }

    void writeTags(String rootPath) {
        String path = rootPath + "/data/minecraft/tags/worldgen/structure";
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        NbtCompound dictionary = CommonIO.read(InfinityMod.utilPath + "/structure_tags.json");
        Map<String, NbtList> tags = new HashMap<>();
        for (String s : structure_ids.keySet()) for (String ss : dictionary.getKeys()) if (s.contains(ss)) {
            for (NbtElement e : (NbtList) Objects.requireNonNull(dictionary.get(ss))) {
                String t = e.asString();
                if (!tags.containsKey(t)) tags.put(t, new NbtList());
                structure_ids.get(s).forEach(fullname -> tags.get(t).add(NbtString.of(fullname)));
            }
        }
        for (String t : tags.keySet()) {
            NbtCompound compound = new NbtCompound();
            compound.putBoolean("replace", false);
            compound.put("values", tags.get(t));
            CommonIO.write(compound, path, t + ".json");
        }
    }
}
