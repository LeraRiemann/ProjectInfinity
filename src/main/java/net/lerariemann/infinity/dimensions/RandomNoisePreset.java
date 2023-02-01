package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.nbt.*;
import org.apache.logging.log4j.LogManager;

import java.util.*;

public class RandomNoisePreset {
    private final RandomProvider PROVIDER;
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
        NbtCompound data = new NbtCompound();
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
        int s = dim.random.nextInt(13);
        noise.putInt("size_horizontal", 1 + (s < 8 ? s/4 : (s == 12 ? 2 : 3)));
        s = dim.random.nextInt(3);
        noise.putInt("size_vertical", 1 + (s == 2 ? s : 3));
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
        for (String key: new String[]{"inline", "special", "surface", "shallow", "second_layer", "deep"}) {
            biomeRegistry.put(key, new HashSet<>());
            NbtCompound full_list = resolve("surface_rule/registry", key);
            for (NbtElement biome : (NbtList) Objects.requireNonNull(full_list.get("elements"))) {
                String biome_name = Objects.requireNonNull(((NbtCompound) biome).get("biome")).toString();
                biome_name = biome_name.substring(1, biome_name.length() - 1);
                LogManager.getLogger().info(biome_name);
                if (parent.vanilla_biomes.contains(biome_name)) {
                    String biome_key = Objects.requireNonNull(((NbtCompound) biome).get("key")).toString();
                    biome_key = biome_key.substring(1, biome_key.length() - 1);
                    regBiome(key, biome_key);
                }
            }
        }
        for (int id: parent.random_biome_ids) {
            String name = "infinity:generated_" + id;
            registerRandomBiome(name);
        }
        for (String key: new String[]{"surface", "shallow", "deep"}) biomeRegistry.get(key).add("default_overworld");
    }

    void registerRandomBiome(String biome) {
        if (parent.random.nextBoolean()) {
            regBiome("surface", biome);
            regBiome("shallow", biome);
        }
        else parent.top_blocks.put(biome, "minecraft:grass_block");
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
        addInline(sequence);
        if (!biomeRegistry.get("special").isEmpty()) sequence.add(getSpecial());
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

    void addInline(NbtList sequence) {
        for (String biome : biomeRegistry.get("inline")) sequence.add(readBiome("inline", biome));
    }

    NbtCompound getSpecial() {
        return conditionType(stoneCondition(false, 0, 0, true), readAllBiomes("special"));
    }

    NbtCompound getSurface() {
        NbtCompound then_run = conditionType(waterCondition(false, -1, 0), readAllBiomes("surface"));
        return conditionType(stoneCondition(false, 0, 0, true), then_run);
    }

    NbtCompound getShallow() {
        NbtList sequence = new NbtList();
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
        else {
            String block = PROVIDER.FULL_BLOCKS.getRandomElement(parent.random);
            if (category.equals("surface")) parent.top_blocks.put(biome, block);
            return biomeCondition(biome, blockType(block));
        }
    }
}
