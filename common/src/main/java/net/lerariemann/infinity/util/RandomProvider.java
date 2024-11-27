package net.lerariemann.infinity.util;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.minecraft.nbt.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.Files.walk;

public class RandomProvider {
    public Map<String, WeighedStructure<String>> registry;
    public Map<String, WeighedStructure<NbtElement>> compoundRegistry;
    public Map<String, Double> rootChances;
    public Map<String, Boolean> gameRules;
    public Map<String, Integer> gameRulesInt;
    public String configPath;
    public String savingPath;
    public String portalKey;
    public String altarKey;

    public NbtCompound noise;
    public String salt;
    public Easterizer easterizer;

    public static RandomProvider getProvider(MinecraftServer server) {
        return ((MinecraftServerAccess)(server)).infinity$getDimensionProvider();
    }

    public RandomProvider(String configpath, String savingpath) {
        this(configpath);
        savingPath = savingpath;
        genCorePack();
    }

    public RandomProvider(String configpath) {
        configPath = configpath;
        registry = new HashMap<>();
        compoundRegistry = new HashMap<>();
        rootChances = new HashMap<>();
        gameRules = new HashMap<>();
        gameRulesInt = new HashMap<>();
        register_all();
        easterizer = new Easterizer(this);
        InfinityMod.longArithmeticEnabled = rule("longArithmeticEnabled");
    }

    void register_all() {
        read_root_config();
        String path = configPath + "modular";
        register_category(registry, path, "misc", CommonIO::stringListReader);
        register_category(registry, path, "features", CommonIO::stringListReader);
        register_category(registry, path, "vegetation", CommonIO::stringListReader);
        register_category(compoundRegistry, path, "blocks", CommonIO::compoundListReader);
        extract_blocks();
        register_category(compoundRegistry, path, "extra", CommonIO::compoundListReader);
        extract_mobs();
        register_category_hardcoded(configPath + "hardcoded");
        noise = CommonIO.read(InfinityMod.utilPath + "/noise.json");
    }

    void read_root_config() {
        NbtCompound rootConfig = CommonIO.read(configPath + "infinity.json");
        portalKey = rootConfig.getString("portalKey");
        altarKey = rootConfig.getString("altarKey");
        salt = rootConfig.getString("salt");
        NbtCompound gamerules = rootConfig.getCompound("gameRules");
        for (String s: gamerules.getKeys()) {
            NbtElement elem = gamerules.get(s);
            if (elem!=null) {
                if (elem.getType() == 3) gameRulesInt.put(s, gamerules.getInt(s));
                else gameRules.put(s, gamerules.getBoolean(s));
            }
        }
        NbtCompound rootchances = rootConfig.getCompound("rootChances");
        for (String c: rootchances.getKeys()) {
            for (String s: rootchances.getCompound(c).getKeys()) {
                rootChances.put(s, rootchances.getCompound(c).getDouble(s));
            }
        }
    }

    public NbtCompound notRandomTree(String tree, String block) {
        return CommonIO.readCarefully(InfinityMod.utilPath + "/placements/tree_vanilla.json", tree, block);
    }

    void saveTrees() {
        List<String> trees = registry.get("trees").keys;
        double size = trees.size();
        NbtCompound c = new NbtCompound();
        NbtList l = new NbtList();
        c.put("default", notRandomTree(trees.get(0), "minecraft:grass_block"));
        for (int i = 1; i < size; i++) {
            NbtCompound c1 = new NbtCompound();
            c1.put("feature", notRandomTree(trees.get(i), "minecraft:grass_block"));
            c1.putDouble("chance", 1 / (size - i + 1));
            l.add(c1);
        }
        c.put("features", l);
        NbtCompound c2 = new NbtCompound();
        c2.putString("type", "minecraft:random_selector");
        c2.put("config", c);
        CommonIO.write(c2, savingPath + "/data/" + InfinityMod.MOD_ID + "/worldgen/configured_feature", "all_trees.json");
    }

    void genCorePack() {
        saveTrees();
        if (!(Paths.get(savingPath + "/pack.mcmeta")).toFile().exists()) savePackMcmeta();
    }

    void savePackMcmeta() {
        NbtCompound res = new NbtCompound();
        NbtCompound pack = new NbtCompound();
        pack.putInt("pack_format", 15);
        pack.putString("description", "Common template pools for Infinite Dimensions");
        res.put("pack", pack);
        CommonIO.write(res, savingPath, "pack.mcmeta");
    }

    public boolean roll(Random random, String key) {
        return (random.nextDouble() < rootChances.get(key));
    }
    public boolean rule(String key) {
        return gameRules.get(key);
    }

    void extract_blocks() {
        if (compoundRegistry.containsKey("blocks")) {
            WeighedStructure<NbtElement> blocksSettings = compoundRegistry.get("blocks");
            WeighedStructure<NbtElement> allBlocks = new WeighedStructure<>();
            WeighedStructure<NbtElement> blocksFeatures = new WeighedStructure<>();
            WeighedStructure<NbtElement> topBlocks = new WeighedStructure<>();
            WeighedStructure<NbtElement> fullBlocks = new WeighedStructure<>();
            WeighedStructure<NbtElement> fullBlocksWG = new WeighedStructure<>();
            for (int i = 0; i < blocksSettings.keys.size(); i++) {
                NbtCompound e = (NbtCompound)(blocksSettings.keys.get(i));
                boolean isfull, istop, isfloat, islaggy;
                isfull = check(e, "full", false);
                islaggy = check(e, "laggy", false);
                isfloat = check(e, "float", isfull);
                istop = check(e, "top", isfull);
                istop = istop || isfloat;
                Double w = blocksSettings.weights.get(i);
                allBlocks.add(e, w);
                if (isfull) fullBlocks.add(e, w);
                if (istop && !islaggy) topBlocks.add(e, w);
                if (isfloat) blocksFeatures.add(e, w);
                if (isfull && isfloat && !islaggy) fullBlocksWG.add(e, w);
            }
            compoundRegistry.put("all_blocks", allBlocks);
            compoundRegistry.put("blocks_features", blocksFeatures);
            compoundRegistry.put("full_blocks", fullBlocks);
            compoundRegistry.put("full_blocks_worldgen", fullBlocksWG);
            compoundRegistry.put("top_blocks", topBlocks);
            compoundRegistry.remove("blocks");
        }
    }

    void extract_mobs() {
        if (compoundRegistry.containsKey("mobs")) {
            WeighedStructure<NbtElement> allmobs = compoundRegistry.get("mobs");
            WeighedStructure<String> allMobNames = new WeighedStructure<>();
            for (int i = 0; i < allmobs.size(); i++) if (allmobs.keys.get(i) instanceof NbtCompound mob) {
                String group = mob.getString("Category");
                if (!registry.containsKey(group)) registry.put(group, new WeighedStructure<>());
                registry.get(group).add(mob.getString("Name"), allmobs.weights.get(i));
                allMobNames.add(mob.getString("Name"), allmobs.weights.get(i));
            }
            registry.put("mobs", allMobNames);
            compoundRegistry.remove("mobs");
        }
    }

    static boolean check(NbtCompound e, String key, boolean def) {
        boolean res = e.contains(key) ? e.getBoolean(key) : def;
        e.remove(key);
        return res;
    }

    static <B> void register_category(Map<String, B> reg, String path, String subpath, ListReader<B> reader) {
        try {
            walk(Paths.get(path + "/minecraft/" + subpath)).forEach(p -> {
                String fullname = p.toString();
                if (p.toFile().isFile()) {
                    String name = fullname.substring(fullname.lastIndexOf("/") + 1, fullname.length() - 5);
                    name = name.substring(name.lastIndexOf('\\') + 1);
                    int i = fullname.replace("minecraft_", "%%%").lastIndexOf("minecraft");
                    String sub = fullname.substring(i+10);
                    reg.put(name, reader.op(path, sub));
                }});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void register_category_hardcoded(String path) {
        try {
            walk(Paths.get(path)).forEach(p -> {
                String fullname = p.toString();
                if (fullname.endsWith(".json")) {
                    String name = fullname.substring(fullname.lastIndexOf("/") + 1, fullname.length() - 5);
                    name = name.substring(name.lastIndexOf('\\') + 1);
                    if (!name.equals("none")) registry.put(name, CommonIO.stringListReader(fullname));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> mob_categories() {
        return registry.get("mob_categories").keys;
    }

    public static NbtCompound Block(String block) {
        NbtCompound res = new NbtCompound();
        res.putString("Name", block);
        return res;
    }

    public static NbtCompound Fluid(String block) {
        NbtCompound res = new NbtCompound();
        res.putString("Name", block);
        res.putString("fluidName", block);
        return res;
    }

    public NbtCompound blockToProvider(NbtCompound block, Random random) {
        NbtCompound res = new NbtCompound();
        boolean isRotatable = Registries.BLOCK.get(new Identifier(block.getString("Name"))).getDefaultState().getProperties().contains(Properties.AXIS);
        res.putString("type", isRotatable && roll(random, "rotate_blocks") ?
                "minecraft:rotated_block_provider" : "minecraft:simple_state_provider");
        res.put("state", block);
        return res;
    }

    public static NbtCompound blockToProvider(NbtCompound block) {
        NbtCompound res = new NbtCompound();
        res.putString("type", "minecraft:simple_state_provider");
        res.put("state", block);
        return res;
    }

    public static String blockElementToName(NbtElement e) {
        if (e instanceof NbtCompound) return ((NbtCompound)e).getString("Name");
        else return e.asString();
    }

    public String randomName(Random random, String key) {
        switch (key) {
            case "all_blocks", "top_blocks", "blocks_features", "full_blocks", "full_blocks_worldgen" -> {
                return blockElementToName(compoundRegistry.get(key).getRandomElement(random));
            }
            default -> {
                return registry.get(key).getRandomElement(random);
            }
        }
    }

    public NbtCompound randomBlock(Random random, String key) {
        if (compoundRegistry.containsKey(key)) {
            NbtElement compound = compoundRegistry.get(key).getRandomElement(random);
            if (compound instanceof NbtCompound) return ((NbtCompound)compound);
            else return Block(compound.asString());
        }
        else {
            return Block(registry.get(key).getRandomElement(random));
        }
    }

    public NbtCompound randomFluid(Random random) {
        NbtElement compound = compoundRegistry.get("fluids").getRandomElement(random);
        if (compound instanceof NbtCompound) return ((NbtCompound)compound);
        else return Fluid(compound.asString());
    }

    public NbtCompound randomBlockProvider(Random random, String key) {
        return blockToProvider(randomBlock(random, key), random);
    }

    public NbtCompound randomPreset(Random random, String key) {
        NbtElement list = compoundRegistry.get("color_presets").getRandomElement(random);
        NbtCompound res = new NbtCompound();
        if (list instanceof NbtList lst) {
            res.putString("type", key);
            if (key.equals("noise_provider")) {
                res.putInt("seed", 0);
                res.putDouble("scale", 4.0);
                res.put("states", lst);
                res.put("noise", noise);
            }
            if (key.equals("weighted_state_provider")) {
                NbtList entries = new NbtList();
                for (NbtElement block : lst) {
                    NbtCompound entry = new NbtCompound();
                    entry.put("data", block);
                    entry.putDouble("weight", 1.0);
                    entries.add(entry);
                }
                res.put("entries", entries);
            }
        }
        return res;
    }

    public static NbtCompound genBounds(int lbound, int bound) {
        NbtCompound value = new NbtCompound();
        value.putInt("min_inclusive", lbound);
        value.putInt("max_inclusive", bound);
        return value;
    }

    public static NbtCompound genBounds(Random random, int lbound, int bound) {
        int a = random.nextInt(lbound, bound);
        int b = random.nextInt(lbound, bound);
        return genBounds(Math.min(a, b), Math.max(a, b));
    }

    public static NbtElement intProvider(Random random, int bound, boolean acceptDistributions) {
        return intProvider(random, 0, bound, acceptDistributions);
    }

    public static NbtElement intProvider(Random random, int lbound, int bound, boolean acceptDistributions) {
        int i = random.nextInt(acceptDistributions ? 6 : 4);
        NbtCompound res = new NbtCompound();
        switch(i) {
            case 0 -> {
                res.putString("type", "constant");
                res.putInt("value", random.nextInt(lbound, bound));
                return res;
            }
            case 1, 2 -> {
                res.putString("type", i==1 ? "uniform" : "biased_to_bottom");
                res.put("value", genBounds(random, lbound, bound));
                return res;
            }
            case 4 -> {
                res.putString("type", "clamped");
                NbtCompound value = genBounds(random, lbound, bound);
                value.put("source", intProvider(random, lbound, bound, false));
                res.put("value", value);
                return res;
            }
            case 3 -> {
                res.putString("type", "clamped_normal");
                NbtCompound value = genBounds(random, lbound, bound);
                value.putDouble("mean", lbound + random.nextDouble()*(bound-lbound));
                value.putDouble("deviation", random.nextExponential());
                res.put("value", value);
                return res;
            }
            case 5 -> {
                res.putString("type", "weighted_list");
                int j = 2 + random.nextInt(0, 5);
                NbtList list = new NbtList();
                for (int k=0; k<j; k++) {
                    NbtCompound element = new NbtCompound();
                    element.put("data", intProvider(random, lbound, bound, false));
                    element.putInt("weight", random.nextInt(100));
                    list.add(element);
                }
                res.put("distribution", list);
                return res;
            }
        }
        return res;
    }

    public static NbtCompound heightProvider(Random random, int lbound, int bound, boolean acceptDistributions, boolean trim) {
        int i = random.nextInt(acceptDistributions ? 6 : 5);
        String[] types = new String[]{"uniform", "biased_to_bottom", "very_biased_to_bottom", "trapezoid", "constant", "weighted_list"};
        NbtCompound res = new NbtCompound();
        res.putString("type", types[i]);
        switch (i) {
            case 4 -> {
                NbtCompound value = new NbtCompound();
                value.putInt("absolute", random.nextInt(lbound, bound));
                res.put("value", value);
            }
            case 0, 1, 2, 3 -> {
                NbtCompound min_inclusive = new NbtCompound();
                NbtCompound max_inclusive = new NbtCompound();
                int min, max;
                if (!trim && (i == 3)) {
                    int center = random.nextInt(lbound, bound);
                    int sigma = random.nextInt(bound-lbound);
                    min = center - sigma;
                    max = center + sigma;
                }
                else {
                    int k = random.nextInt(lbound, bound);
                    int j = random.nextInt(lbound, bound);
                    min = Math.min(k, j);
                    max = Math.max(k, j);
                }
                min_inclusive.putInt("absolute", min);
                max_inclusive.putInt("absolute", max);
                res.put("min_inclusive", min_inclusive);
                res.put("max_inclusive", max_inclusive);
                if (i==3 && random.nextBoolean()) res.putInt("plateau", random.nextInt(1, max - min));
                else if (i!=0) res.putInt("inner", 1 + (int)Math.floor(random.nextExponential()));
            }
            case 5 -> {
                int j = 2 + random.nextInt(0, 5);
                NbtList list = new NbtList();
                for (int k=0; k<j; k++) {
                    NbtCompound element = new NbtCompound();
                    element.put("data", heightProvider(random, lbound, bound, false, trim));
                    element.putInt("weight", random.nextInt(100));
                    list.add(element);
                }
                res.put("distribution", list);
            }
        }
        return res;
    }

    public static NbtCompound floatProvider(Random random, float lbound, float bound) {
        int i = random.nextInt(3);
        String[] types = new String[]{"uniform", "clamped_normal", "trapezoid"};
        NbtCompound res = new NbtCompound();
        res.putString("type", types[i]);
        NbtCompound value = new NbtCompound();
        float a = random.nextFloat(lbound, bound);
        float b = random.nextFloat(lbound, bound);
        float min = Math.min(a, b);
        float max = Math.max(a, b);
        switch (i) {
            case 0 -> {
                value.putFloat("max_exclusive", max);
                value.putFloat("min_inclusive", min);
            }
            case 1 -> {
                value.putFloat("max", max);
                value.putFloat("min", min);
                value.putFloat("mean", random.nextFloat(min, max));
                value.putFloat("deviation", random.nextFloat(max - min));
            }
            case 2 -> {
                value.putFloat("max", max);
                value.putFloat("min", min);
                value.putFloat("plateau", random.nextFloat(max - min));
            }
        }
        res.put("value", value);
        return res;
    }

    public void kickGhostsOut(DynamicRegistryManager s) {
        Registry<Biome> reg = s.get(RegistryKeys.BIOME);
        WeighedStructure<String> biomes = registry.get("biomes");
        int i = 0;
        while(i < biomes.keys.size()) {
            if (!reg.containsId(new Identifier(biomes.keys.get(i)))) {
                biomes.kick(i);
            }
            else i++;
        }
        registry.put("biomes", biomes);
    }
}

interface ListReader<B>  {
    B op(String path, String subpath);
}
