package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.Files.walk;

public class RandomNoisePreset {
    private final RandomProvider PROVIDER;
    public String name;
    public String fullname;
    public RandomDimension parent;
    public String noise_router, surface_rule, spawn_target, type_alike;
    int sea_level_default;
    Map<String,Set<String>> biomeRegistry;
    public boolean randomiseblocks;

    RandomNoisePreset(RandomDimension dim) {
        parent = dim;
        biomeRegistry = new HashMap<>();
        PROVIDER = dim.PROVIDER;
        name = "generated_" +dim.id;
        fullname = InfinityMod.MOD_ID + ":" + name;
        randomiseblocks = PROVIDER.roll(dim.random, "randomise_blocks");
        NbtCompound data = new NbtCompound();
        type_alike = dim.type_alike;
        String typeshort = type_alike.substring(type_alike.lastIndexOf(":") + 1);
        if (!dim.isNotOverworld()) {
            noise_router = typeshort;
            surface_rule = spawn_target = "overworld";
            data.putBoolean("aquifers_enabled", true);
            sea_level_default = 63;
        }
        else {
            noise_router = surface_rule = typeshort;
            data.putBoolean("aquifers_enabled", false);
            spawn_target = "default";
            switch (type_alike) {
                case "minecraft:floating_islands" -> sea_level_default = -64;
                case "minecraft:end" -> sea_level_default = 0;
                case "minecraft:nether", "minecraft:caves" -> {
                    sea_level_default = 32;
                    noise_router = "caves";
                }
            }
        }
        int sea_level = randomiseblocks ? (int)Math.floor(dim.random.nextGaussian(sea_level_default, 8)) : sea_level_default;
        NbtCompound default_block = randomiseblocks ? PROVIDER.randomBlock(dim.random, "full_blocks_worldgen") : RandomProvider.Block(defaultblock("minecraft:stone"));
        NbtCompound default_fluid = randomiseblocks ? PROVIDER.randomBlock(dim.random,
                PROVIDER.roll(dim.random, "solid_oceans") ? "full_blocks_worldgen" : "fluids") : RandomProvider.Block(defaultfluid());
        data.putBoolean("ore_veins_enabled", dim.random.nextBoolean());
        data.putBoolean("disable_mob_generation", false);
        data.putBoolean("legacy_random_source", false);
        data.put("default_block", default_block);
        parent.default_block = default_block;
        data.put("default_fluid", default_fluid);
        data.putInt("sea_level", sea_level);
        parent.sea_level = sea_level;
        data.put("noise", noise(dim));
        data.put("noise_router", getRouter(noise_router));
        data.put("spawn_target", CommonIO.read(PROVIDER.configPath + "util/spawn_target/" + spawn_target + ".json").get("spawn_target"));
        data.put("surface_rule", buildSurfaceRule());
        CommonIO.write(data, dim.storagePath + "/worldgen/noise_settings", name + ".json");
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

    String defaultblock(String s) {
        switch(type_alike) {
            case "minecraft:end" -> {
                return "minecraft:end_stone";
            }
            case "minecraft:nether" -> {
                return "minecraft:netherrack";
            }
            default -> {
                return s;
            }
        }
    }
    String defaultfluid() {
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

    NbtCompound getRouter(String router) {
        String path = PROVIDER.configPath + "util/noise_router/" + router + ".json";
        int min = parent.min_y;
        int max = parent.height + parent.min_y;
        int softmax = Math.min(max, 256);
        switch(router) {
            case "caves" -> {
                return CommonIO.readCarefully(path, min - 8, min + 24, max - 24, max);
            }
            case "floating_islands" -> {
                return CommonIO.readCarefully(path, min + 4, min + 32, max - 72, max + 184);
            }
            case "end" -> {
                return CommonIO.readCarefully(path, min + 4, min + 32, max - 72, max + 184, min + 4, min + 32, max - 72, max + 184);
            }
            case "overworld", "large_biomes" -> {
                return CommonIO.readCarefully(path, min, min + 24, softmax - 16, softmax, min, max,
                        (float)(max+1), (float)(min+4), (float)(max+1), (float)(min+4), (float)(max+1), (float)(min+4), (float)(max+1), (float)(min+4),
                        min, min + 24, softmax - 16, softmax);
            }
            case "amplified" -> {
                return CommonIO.readCarefully(path, min, min + 24, max - 16, max, min, max,
                        (float)(max+1), (float)(min+4), (float)(max+1), (float)(min+4), (float)(max+1), (float)(min+4), (float)(max+1), (float)(min+4),
                        min, min + 24, max - 16, max);
            }
            case "whack" -> {
                double f = parent.random.nextExponential();
                return CommonIO.readCarefully(path, min, max, min, max,
                        2*f, min, min+8, max-8, -2*f, max,
                        min, parent.sea_level, parent.sea_level, max,
                        f, parent.random.nextDouble(1.0, 8.0), parent.random.nextDouble(1.0, 8.0));
            }
        }
        return CommonIO.read(path);
    }

    NbtCompound buildSurfaceRule() {
        int i = 0;
        switch (surface_rule) {
            case "caves", "nether" -> i=1;
            case "floating_islands", "end" -> i=2;
        }
        NbtCompound res = startingRule("sequence");
        NbtList sequence = new NbtList();
        if (i!=2) sequence.add(CommonIO.read(PROVIDER.configPath + "util/surface_rule/bedrock_floor.json"));
        if (i==1) sequence.add(CommonIO.read(PROVIDER.configPath + "util/surface_rule/bedrock_roof.json"));
        sequence.add(getBiomes(i==0));
        if (i==0) addDeepslate(sequence);
        res.put("sequence", sequence);
        return res;
    }

    void addDeepslate(NbtList base) {
        NbtCompound deepslate = randomiseblocks ? PROVIDER.randomBlock(parent.random, "full_blocks_worldgen") :
                RandomProvider.Block("minecraft:deepslate");
        base.add(CommonIO.readAndAddBlock(PROVIDER.configPath + "util/surface_rule/deepslate.json", deepslate));
        parent.additional_blocks.add(deepslate);
    }

    NbtCompound startingRule(String str) {
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

    NbtList biomeSequence() {
        NbtList sequence = new NbtList();
        try {
            walk(Paths.get(PROVIDER.configPath + "modular/")).forEach(p -> {
                if (p.toString().contains("surface_rule") && p.toFile().isFile()) {
                    NbtCompound compound = CommonIO.readSurfaceRule(p.toFile(), parent.sea_level);
                    NbtList biomes = compound.getList("biomes", NbtElement.STRING_TYPE);
                    NbtList biomestoadd = new NbtList();
                    for (NbtElement b : biomes) {
                        if (parent.vanilla_biomes.contains(b.asString())) biomestoadd.add(b);
                    }
                    if (!biomestoadd.isEmpty()) {
                        NbtCompound rule = compound.getCompound("rule");
                        sequence.add(ruleWrap(biomestoadd, rule));
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int id: parent.random_biome_ids) {
            String biome = "infinity:biome_" + id;
            boolean useRandomBlock = randomiseblocks && PROVIDER.roll(parent.random, "randomise_biome_blocks");
            NbtCompound top_block = useRandomBlock ? PROVIDER.randomBlock(parent.random, "top_blocks") : RandomProvider.Block(defaultblock("minecraft:grass_block"));
            parent.top_blocks.put(biome, top_block);
            NbtCompound block_underwater = useRandomBlock ? PROVIDER.randomBlock(parent.random, "full_blocks_worldgen") : RandomProvider.Block(defaultblock("minecraft:dirt"));
            parent.underwater.put(biome, block_underwater);
            NbtCompound rule = CommonIO.readCarefully(PROVIDER.configPath + "util/surface_rule/custom.json",
                    CommonIO.CompoundToString(top_block, 8), CommonIO.CompoundToString(block_underwater, 7), CommonIO.CompoundToString(block_underwater, 9),
                    CommonIO.CompoundToString(parent.default_block, 7), CommonIO.CompoundToString(block_underwater, 6));
            NbtList biomestoadd = new NbtList();
            biomestoadd.add(NbtString.of(biome));
            sequence.add(ruleWrap(biomestoadd, rule));
        }
        return sequence;
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
