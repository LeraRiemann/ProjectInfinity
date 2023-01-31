package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.nbt.*;
import java.util.*;

public class RandomNoisePreset {
    private NbtCompound data;
    private int id;
    private RandomProvider PROVIDER;
    public String name;
    public String fullname;
    String storagePath;
    public RandomDimension parent;
    String noise_router, surface_rule, spawn_target;
    int sea_level_default;
    Map<String,Set<String>> biomeRegistry;

    RandomNoisePreset(RandomDimension dim) {
        parent = dim;
        biomeRegistry = new HashMap<>();
        PROVIDER = dim.PROVIDER;
        storagePath = PROVIDER.configPath + "noise_settings/";
        name = "generated_" +dim.id;
        fullname = InfinityMod.MOD_ID + ":" + name;
        data = new NbtCompound();
        String type_alike = PROVIDER.NOISE_PRESETS.getRandomElement(dim.random);
        switch (type_alike) {
            case "minecraft:overworld", "minecraft:amplified", "minecraft:large_biomes" -> {
                noise_router = type_alike.substring(10);
                surface_rule = spawn_target = "overworld";
                sea_level_default = 63;
            }
            case "minecraft:floating_islands", "minecraft:caves", "minecraft:end", "minecraft:nether" -> {
                noise_router = surface_rule = type_alike.substring(10);
                spawn_target = "default";
                switch (type_alike) {
                    case "minecraft:floating_islands" -> sea_level_default = -64;
                    case "minecraft:end" -> sea_level_default = 0;
                    case "minecraft:nether", "minecraft:caves" -> sea_level_default = 32;
                }
            }
        }
        int sea_level = (int)Math.floor(dim.random.nextGaussian(sea_level_default, 8));
        NbtCompound default_block = PROVIDER.randomBlock(dim.random, PROVIDER.FULL_BLOCKS);
        WeighedStructure<String> whereFrom = RandomProvider.weighedRandom(dim.random, 1, 15) ? PROVIDER.FLUIDS : PROVIDER.FULL_BLOCKS;
        NbtCompound default_fluid = PROVIDER.randomBlock(dim.random, whereFrom);
        data.putBoolean("aquifers_enabled", dim.random.nextBoolean());
        data.putBoolean("ore_veins_enabled", dim.random.nextBoolean());
        data.putBoolean("disable_mob_generation", false);
        data.putBoolean("legacy_random_source", false);
        data.put("default_block", default_block);
        data.put("default_fluid", default_fluid);
        data.putInt("sea_level", sea_level);
        NbtCompound noise = new NbtCompound();
        noise.putInt("height", dim.height);
        noise.putInt("min_y", dim.min_y);
        noise.putInt("size_horizontal", dim.random.nextInt(5));
        noise.putInt("size_vertical", dim.random.nextInt(5));
        data.put("noise", noise);
        data.put("noise_router", resolve("noise_router", noise_router));
        data.put("spawn_target", resolve("spawn_target", spawn_target).get("spawn_target"));
        registerBiomes();
        data.put("surface_rule", buildSurfaceRule(surface_rule));
        CommonIO.write(data, dim.storagePath + "/worldgen/noise_settings", name + ".json");
    }

    NbtCompound resolve(String type, String name) {
        return CommonIO.read(storagePath + type + "/" + name + ".json");
    }

    void registerBiomes() {
        for (String key: new String[]{"nether", "special", "badlands", "surface", "frozen", "shallow", "second_layer", "deep"}) biomeRegistry.put(key, new HashSet<>());
        for (String biome: parent.biomes) {
            if (biome.startsWith("m")) registerVanillaBiome(biome.substring(10));
            else registerRandomBiome(biome);
        }
        for (String key: new String[]{"surface", "shallow", "deep"}) biomeRegistry.get(key).add("default_overworld");
    }

    void registerVanillaBiome(String biome) {
        switch (biome) {
            case "basalt_deltas", "soul_sand_valley", "nether_wastes" -> biomeRegistry.get("nether").add(biome);
            case "wooded_badlands", "swamp", "crimson_forest", "warped_forest" -> biomeRegistry.get("special").add(biome);
            case "mangrove_swamp" -> {
                regBiome("special", biome);
                regBiome("surface", biome);
                regBiome("shallow", biome);
            }
            case "warm_ocean", "beach", "snowy_beach", "desert" -> {
                String biomename = biome.equals("desert") ? biome : "warm_ocean_and_beaches";
                regBiome("surface", biomename);
                regBiome("shallow", biomename);
                regBiome("second_layer", biomename);
            }
            case "frozen_peaks", "snowy_slopes", "jagged_peaks", "grove", "stony_peaks", "stony_shore", "windswept_hills",
                    "dripstone_caves", "windswept_savanna", "windswept_gravelly_hills" -> {
                regBiome("surface", biome);
                regBiome("shallow", biome);
            }
            case "frozen_ocean", "deep_frozen_ocean" -> {
                regBiome("surface", "frozen_oceans");
                regBiome("frozen", "frozen_oceans");
            }
            case "old_growth_pine_taiga", "old_growth_spruce_taiga" -> regBiome("surface", "old_taigas");
            case "ice_spikes", "mushroom_fields" -> regBiome("surface", biome);
        }
        switch (biome) {
            case "wooded_badlands", "eroded_badlands", "badlands" -> regBiome("badlands", "badlands");
            case "frozen_peaks", "jagged_peaks" -> regBiome("deep", "peaks");
            case "warm_ocean", "lukewarm_ocean", "deep_lukewarm_ocean" -> regBiome("deep", "warmer_oceans");
        }
    }

    void registerRandomBiome(String biome) {
        if (parent.random.nextBoolean()) {
            regBiome("surface", biome);
            if (parent.random.nextBoolean()) regBiome("shallow", biome);
        }
    }

    void regBiome(String type, String name) {
        biomeRegistry.get(type).add(name);
    }

    NbtCompound buildSurfaceRule(String surface_rule_base) {
        int i = 0;
        switch (surface_rule_base) {
            case "caves", "nether" -> i=1;
            case "floating_islands", "end" -> i=2;
        }
        NbtCompound res = startingRule("sequence");
        NbtList sequence = new NbtList();
        if (i!=2) addFloor(sequence);
        if (i==1) addRoof(sequence);
        sequence.add(getBiomes(i==0));
        if (i==0) addDeepslate(sequence);
        res.put("sequence", sequence);
        return res;
    }

    void addFloor(NbtList base) {
        base.add(resolve("surface_rule", "main/bedrock_floor"));
    }
    void addRoof(NbtList base) {
        base.add(resolve("surface_rule", "main/bedrock_roof"));
    }

    void addDeepslate(NbtList base) {
        base.add(CommonIO.readAndAddBlock(storagePath + "surface_rule/main/deepslate.json", PROVIDER.FULL_BLOCKS.getRandomElement(parent.random)));
    }

    void addType(NbtCompound base, String str) {
        base.putString("type", "minecraft:" + str);
    }

    NbtCompound startingRule(String str) {
        NbtCompound res = new NbtCompound();
        addType(res, str);
        return res;
    }

    NbtCompound getBiomes(boolean usePreliminarySurface) {
        if (usePreliminarySurface) {
            NbtCompound res = startingRule("condition");
            res.put("if_true", startingRule("above_preliminary_surface"));
            res.put("then_run", getBiomes(false));
            return res;
        }
        NbtCompound res = startingRule("sequence");
        NbtList sequence = new NbtList();
        addNether(sequence);
        if (!biomeRegistry.get("special").isEmpty()) sequence.add(getSpecial());
        if (!biomeRegistry.get("badlands").isEmpty()) addBadlands(sequence);
        sequence.add(getSurface());
        sequence.add(getShallow());
        sequence.add(getDeep());
        res.put("sequence", sequence);
        return res;
    }

    NbtCompound stoneCondition(boolean add_surface_depth, int offset, int secondary_depth_range, boolean floor) {
        NbtCompound res = startingRule("stone_depth");
        res.putBoolean("add_surface_depth", add_surface_depth);
        res.putInt("offset", offset);
        res.putInt("secondary_depth_range", secondary_depth_range);
        res.putString("surface_type", floor ? "floor" : "ceiling");
        return res;
    }

    NbtCompound waterCondition(boolean add_stone_depth, int offset, int surface_depth_multiplier) {
        NbtCompound res = startingRule("water");
        res.putBoolean("add_stone_depth", add_stone_depth);
        res.putInt("offset", offset);
        res.putInt("surface_depth_multiplier", surface_depth_multiplier);
        return res;
    }

    NbtCompound conditionType(NbtCompound if_true, NbtCompound then_run) {
        NbtCompound res = startingRule("condition");
        res.put("if_true", if_true);
        res.put("then_run", then_run);
        return res;
    }

    NbtCompound sequenceType(NbtList sequence) {
        NbtCompound res = startingRule("sequence");
        res.put("sequence", sequence);
        return res;
    }

    NbtCompound readAllBiomes(String category) {
        NbtList sequence = new NbtList();
        for (String biome : biomeRegistry.get(category)) sequence.add(readBiome(category, biome));
        return sequenceType(sequence);
    }

    void addNether(NbtList sequence) {
        for (String biome : biomeRegistry.get("nether")) sequence.add(readBiome("nether", biome));
    }

    NbtCompound getSpecial() {
        return conditionType(stoneCondition(false, 0, 0, true), readAllBiomes("special"));
    }
    void addBadlands(NbtList sequence) {
        sequence.add(readBiome("badlands", "badlands"));
    }

    NbtCompound getSurface() {
        NbtCompound then_run = conditionType(waterCondition(false, -1, 0), readAllBiomes("surface"));
        return conditionType(stoneCondition(false, 0, 0, true), then_run);
    }

    NbtCompound getShallow() {
        NbtList sequence = new NbtList();
        for (String biome : biomeRegistry.get("frozen")) sequence.add(readBiome("frozen", biome));
        sequence.add(conditionType(stoneCondition(true, 0, 0, true), readAllBiomes("shallow")));
        for (String biome : biomeRegistry.get("second_layer")) sequence.add(readBiome("second_layer", biome));
        return conditionType(waterCondition(true, -6, -1), sequenceType(sequence));
    }

    NbtCompound getDeep() {
        return conditionType(stoneCondition(false, 0,0, true), readAllBiomes("deep"));
    }

    NbtCompound biomeCondition(String biome, NbtCompound then_run) {
        NbtCompound if_true = startingRule("biome");
        NbtList biome_is = new NbtList();
        biome_is.add(NbtString.of(biome));
        if_true.put("biome_is", biome_is);
        return conditionType(if_true, then_run);
    }

    NbtCompound blockType(String block) {
        NbtCompound res = startingRule("block");
        NbtCompound result_state = RandomProvider.Block(block);
        res.put("result_state", result_state);
        return res;
    }

    NbtCompound readBiome(String category, String biome) {
        if (!biome.startsWith("infinity:")) return resolve("surface_rule/" + category, biome);
        else return biomeCondition(biome, blockType(PROVIDER.FULL_BLOCKS.getRandomElement(parent.random)));
    }
}
