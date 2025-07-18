package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.lerariemann.infinity.util.core.RandomProvider;
import net.minecraft.nbt.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.Files.walk;

public class RandomNoisePreset {
    private final RandomProvider PROVIDER;
    public String name;
    public String fullname;
    public RandomDimension parent;
    public String noise_router, surface_rule, spawn_target, type_alike;
    Map<String,Set<String>> biomeRegistry;

    RandomNoisePreset(RandomDimension dim) {
        parent = dim;
        biomeRegistry = new HashMap<>();
        PROVIDER = dim.PROVIDER;
        name = "generated_" +dim.numericId;
        fullname = InfinityMod.MOD_ID + ":" + name;
        NbtCompound data = new NbtCompound();
        type_alike = dim.type_alike;
        String typeshort = type_alike.substring(type_alike.lastIndexOf(":") + 1);
        if (dim.isOverworldLike()) {
            noise_router = typeshort;
            surface_rule = spawn_target = "overworld";
            data.putBoolean("aquifers_enabled", true);
        }
        else {
            noise_router = surface_rule = typeshort;
            data.putBoolean("aquifers_enabled", false);
            spawn_target = "default";
            switch (type_alike) {
                case "minecraft:nether", "minecraft:caves" -> noise_router = "caves";
            }
        }
        data.putBoolean("ore_veins_enabled", dim.random.nextBoolean());
        data.putBoolean("disable_mob_generation", false);
        data.putBoolean("legacy_random_source", false);
        data.put("default_block", parent.default_block);
        data.put("default_fluid", NbtUtils.nameToElement(NbtUtils.getString(parent.default_fluid, "Name")));
        data.putInt("sea_level", parent.sea_level);
        data.put("noise", noise(dim));
        data.put("noise_router", getRouter(noise_router));
        data.put("spawn_target", CommonIO.read(InfinityMod.utilPath + "/spawn_target/" + spawn_target + ".json").get("spawn_target"));
        data.put("surface_rule", buildSurfaceRule());
        CommonIO.write(data, dim.getStoragePath() + "/worldgen/noise_settings", name + ".json");
    }

    NbtCompound noise(RandomDimension dim) {
        NbtCompound noise = new NbtCompound();
        noise.putInt("height", dim.height);
        noise.putInt("min_y", dim.min_y);
        boolean rifts = PROVIDER.roll(dim.random, "rift_world_chance");
        int s;
        if (rifts) noise.putInt("size_horizontal", 3);
        else {
            s = dim.random.nextInt(1, 4);
            noise.putInt("size_horizontal", (s == 3 ? 4 : s));
        }
        s = dim.random.nextInt(1, 4);
        noise.putInt("size_vertical", (s == 3 ? 4 : s));
        return noise;
    }

    NbtCompound getRouter(String router) {
        String path = InfinityMod.utilPath + "/noise_router/" + router + ".json";
        int min = parent.min_y;
        int max = parent.height + parent.min_y;
        int softmax = Math.min(max, 256);
        Random r = parent.random;
        switch(router) {
            case "caves" -> {
                return CommonIO.readAndFormat(path, min - 8, min + 24, max - 24, max);
            }
            case "floating_islands" -> {
                return CommonIO.readAndFormat(path, min + 4, min + 32, max - 72, max + 184);
            }
            case "end" -> {
                return CommonIO.readAndFormat(path, min + 4, min + 32, max - 72, max + 184, min + 4, min + 32, max - 72, max + 184);
            }
            case "overworld", "large_biomes" -> {
                return CommonIO.readAndFormat(path, min, min + 24, softmax - 16, softmax, min, max,
                        (float)(max+1), (float)(min+4), (float)(max+1), (float)(min+4), (float)(max+1), (float)(min+4), (float)(max+1), (float)(min+4),
                        min, min + 24, softmax - 16, softmax);
            }
            case "amplified" -> {
                return CommonIO.readAndFormat(path, min, min + 24, max - 16, max, min, max,
                        (float)(max+1), (float)(min+4), (float)(max+1), (float)(min+4), (float)(max+1), (float)(min+4), (float)(max+1), (float)(min+4),
                        min, min + 24, max - 16, max);
            }
            case "whack" -> {
                double f = r.nextExponential();
                double a = r.nextDouble(1.0, 8.0);
                double b = r.nextDouble(1.0, 8.0);
                return CommonIO.readAndFormat(path,
                        2*f, min, min+8, max-8, -2*f, max,
                        min, parent.sea_level, parent.sea_level, max,
                        f, a, b,
                        min, max,
                        2*f, min, min+8, max-8, -2*f, max,
                        min, parent.sea_level, parent.sea_level, max,
                        f, a, b);
            }
            case "tangled" -> {
                double f = r.nextDouble(0.005, 0.1);
                double a = r.nextExponential();
                double b = r.nextExponential();
                return CommonIO.readAndFormat(path, min+32, min, min, max, a, b, a, b, f, min+16, min, max-16, max);
            }
        }
        return CommonIO.read(path);
    }

    NbtCompound buildSurfaceRule() {
        parent.deepslate = parent.randomiseblocks ? PROVIDER.randomElement(parent.random, ConfigType.FULL_BLOCKS_WG) :
                NbtUtils.nameToElement("minecraft:deepslate");
        int i = 0;
        switch (surface_rule) {
            case "caves", "nether", "tangled" -> i=1;
            case "floating_islands", "end" -> i=2;
        }
        NbtCompound res = startingRule("sequence");
        NbtList sequence = new NbtList();
        if (i!=2) sequence.add(CommonIO.read(InfinityMod.utilPath + "/surface_rule/bedrock_floor.json"));
        if (i==1) sequence.add(CommonIO.read(InfinityMod.utilPath + "/surface_rule/bedrock_roof.json"));
        sequence.add(getBiomes(i==0));
        if (i==0) addDeepslate(sequence);
        res.put("sequence", sequence);
        return res;
    }

    void addDeepslate(NbtList base) {
        base.add(CommonIO.readAndAddCompound(InfinityMod.utilPath + "/surface_rule/deepslate.json", parent.deepslate));
        parent.additional_blocks.add(parent.deepslate);
    }

    public static NbtCompound startingRule(String str) {
        NbtCompound res = new NbtCompound();
        res.putString("type", "minecraft:" + str);
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
        NbtList sequence = biomeSequence();
        res.put("sequence", sequence);
        return res;
    }

    NbtCompound randomBlock(ConfigType s) {
        return PROVIDER.randomElement(parent.random, s);
    }

    NbtList biomeSequence() {
        NbtList sequence = new NbtList();
        try (Stream<Path> files = walk(InfinityMod.configPath.resolve("modular"))) {
            files.forEach(p -> {
                if (p.toString().contains("surface_rule") && p.toFile().isFile()) {
                    NbtCompound compound = CommonIO.readSurfaceRule(p.toFile(), parent.sea_level);
                    NbtList biomes = NbtUtils.getList(compound, "biomes", NbtElement.STRING_TYPE);
                    NbtList biomestoadd = new NbtList();
                    for (NbtElement b : biomes) {
                        if (parent.vanilla_biomes.contains(b.asString())) biomestoadd.add(b);
                    }
                    if (!biomestoadd.isEmpty()) {
                        NbtCompound rule = NbtUtils.getCompound(compound, "rule");
                        sequence.add(ruleWrap(biomestoadd, rule));
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (long id: parent.random_biome_ids) {
            String biome = "infinity:biome_" + id;
            String root = InfinityMod.utilPath + "/surface_rule/custom/";
            boolean useRandomBlock = parent.randomiseblocks && PROVIDER.roll(parent.random, "randomise_biome_blocks");
            NbtCompound top_block = useRandomBlock ?
                    randomBlock(RandomProvider.rule("forceSolidSurface") ? ConfigType.FULL_BLOCKS_WG : ConfigType.TOP_BLOCKS) :
                    NbtUtils.nameToElement(parent.getDefaultBlock("minecraft:grass_block"));
            parent.top_blocks.put(biome, top_block);
            NbtCompound block_underwater = useRandomBlock ? randomBlock(ConfigType.FULL_BLOCKS_WG) :
                    NbtUtils.nameToElement(parent.getDefaultBlock("minecraft:dirt"));
            parent.underwater.put(biome, block_underwater);
            NbtCompound beach = useRandomBlock ? randomBlock(ConfigType.FULL_BLOCKS_WG) : top_block;
            NbtCompound rule1 = CommonIO.readAndFormat(root + "ceiling.json",
                    CommonIO.compoundToString(parent.deepslate, 5), CommonIO.compoundToString(parent.default_block, 4));
            NbtCompound rule2 = CommonIO.readAndFormat(root + "grass.json",
                    parent.sea_level - 1, parent.sea_level, CommonIO.compoundToString(beach, 10),
                    CommonIO.compoundToString(top_block, 8), CommonIO.compoundToString(block_underwater, 5));
            NbtCompound rule3 = CommonIO.readAndFormat(root + "dirt.json",
                    CommonIO.compoundToString(block_underwater, 7));
            NbtCompound rule4 = CommonIO.readAndFormat(root + "final.json",
                    CommonIO.compoundToString(parent.deepslate, 5), CommonIO.compoundToString(parent.default_block, 4));
            NbtCompound rule = startingRule("sequence");
            NbtList sq = new NbtList();
            sq.add(rule1);
            sq.add(rule2);
            sq.add(rule3);
            sq.add(rule4);
            rule.put("sequence", sq);
            NbtList biomestoadd = new NbtList();
            biomestoadd.add(NbtString.of(biome));
            sequence.add(ruleWrap(biomestoadd, rule));
        }
        sequence.add(NbtUtils.getCompound(CommonIO.readAndFormat(
                InfinityMod.utilPath + "/surface_rule/default.json",
                defaultBlock("minecraft:grass_block"),
                defaultBlock("minecraft:dirt"),
                defaultBlock("minecraft:dirt"),
                defaultBlock("minecraft:stone"),
                defaultBlock("minecraft:gravel")), "rule"));
        return sequence;
    }

    String defaultBlock(String def) {
        return switch (type_alike) {
            case "minecraft:nether" -> "minecraft:netherrack";
            case "minecraft:end" -> "minecraft:end_stone";
            default -> def;
        };
    }

    NbtCompound ruleWrap(NbtList biomes, NbtCompound rule) {
        NbtCompound res = startingRule("condition");
        NbtCompound if_true = startingRule("biome");
        if_true.put("biome_is", biomes);
        res.put("if_true", if_true);
        res.put("then_run", rule);
        return res;
    }
}
