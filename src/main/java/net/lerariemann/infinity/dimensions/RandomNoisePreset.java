package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.*;

import java.util.*;

public class RandomNoisePreset {
    private final RandomProvider PROVIDER;
    public String name;
    public String fullname;
    String storagePath;
    public RandomDimension parent;
    public String noise_router, surface_rule, spawn_target, type_alike;
    int sea_level_default;
    Map<String,Set<String>> biomeRegistry;
    public boolean randomiseblocks;

    RandomNoisePreset(RandomDimension dim) {
        parent = dim;
        biomeRegistry = new HashMap<>();
        PROVIDER = dim.PROVIDER;
        storagePath = PROVIDER.configPath + "noise_settings/";
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
        data.put("spawn_target", resolve("spawn_target", spawn_target).get("spawn_target"));
        registerBiomes();
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
        String path = storagePath + "noise_router/" + router + ".json";
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
                return CommonIO.readCarefully(path, min, max, min, max, min, parent.sea_level, parent.sea_level, max,
                        parent.random.nextExponential(), parent.random.nextDouble(1.0, 8.0), parent.random.nextDouble(1.0, 8.0));
            }
        }
        return CommonIO.read(path);
    }

    NbtCompound resolve(String type, String name) {
        return CommonIO.read(storagePath + type + "/" + name + ".json");
    }

    void registerBiomes() {
        for (String key: new String[]{"inline", "special", "surface", "shallow", "second_layer", "deep"}) {
            biomeRegistry.put(key, new HashSet<>());
            if ((Objects.equals(parent.type_alike, "minecraft:nether")) && ((Objects.equals(key, "inline")) || (Objects.equals(key, "special"))))
                biomeRegistry.get(key).add("default_nether");
            NbtCompound full_list = resolve("surface_rule/registry", key);
            for (NbtElement biome : (NbtList) Objects.requireNonNull(full_list.get("elements"))) {
                String biome_name = Objects.requireNonNull(((NbtCompound) biome).get("biome")).asString();
                if (parent.vanilla_biomes.contains(biome_name)) regBiome(key, Objects.requireNonNull(((NbtCompound) biome).get("key")).asString());
            }
        }
        for (int id: parent.random_biome_ids) {
            String name = "infinity:biome_" + id;
            registerRandomBiome(name);
        }
    }

    void registerRandomBiome(String biome) {
        regBiome("surface", biome);
        regBiome("shallow", biome);
        boolean useRandomBlock = randomiseblocks && PROVIDER.roll(parent.random, "randomise_biome_blocks");
        NbtCompound top_block = useRandomBlock ? PROVIDER.randomBlock(parent.random, "top_blocks") : RandomProvider.Block(defaultblock("minecraft:grass_block"));
        parent.top_blocks.put(biome, top_block);
        NbtCompound block_underwater = useRandomBlock ? PROVIDER.randomBlock(parent.random, "full_blocks_worldgen") : RandomProvider.Block(defaultblock("minecraft:dirt"));
        parent.underwater.put(biome, block_underwater);
    }

    void regBiome(String type, String name) {
        biomeRegistry.get(type).add(name);
    }

    NbtCompound buildSurfaceRule() {
        int i = 0;
        switch (surface_rule) {
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
        NbtCompound deepslate = randomiseblocks ? PROVIDER.randomBlock(parent.random, "full_blocks_worldgen") :
                RandomProvider.Block("minecraft:deepslate");
        base.add(CommonIO.readAndAddBlock(storagePath + "surface_rule/main/deepslate.json", deepslate));
        parent.additional_blocks.add(deepslate);
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
        if (Objects.equals(category, "surface") || Objects.equals(category, "shallow") || Objects.equals(category, "deep")) {
            sequence.add(readBiome(category, "default_overworld"));
        }
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

    NbtCompound blockType(NbtCompound block) {
        NbtCompound res = startingRule("block");
        res.put("result_state", block);
        return res;
    }

    NbtCompound readBiome(String category, String biome) {
        if (!biome.startsWith("infinity:")) return resolve("surface_rule/" + category, biome);
        else {
            NbtCompound block;
            if (category.equals("surface")) block = parent.top_blocks.get(biome);
            else block = parent.underwater.get(biome);
            return biomeCondition(biome, blockType(block));
        }
    }
}
