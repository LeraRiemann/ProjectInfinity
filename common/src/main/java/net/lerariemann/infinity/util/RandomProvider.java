package net.lerariemann.infinity.util;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.item.Item;
import net.minecraft.nbt.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
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
    public String salt;
    public Easterizer easterizer;

    public RandomProvider(String configpath, String savingpath) {
        this(configpath);
        savingPath = savingpath;
        genCorePack();
    }

    public RandomProvider(String configpath) {
        configPath = configpath;
        initStorage();
        register_all();
        easterizer = new Easterizer(this);
    }

    void initStorage() {
        registry = new HashMap<>();
        compoundRegistry = new HashMap<>();
        rootChances = new HashMap<>();
        gameRules = new HashMap<>();
        gameRulesInt = new HashMap<>();
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

    public Optional<Item> getPortalKeyAsItem() {
        if (portalKey.isBlank()) return Optional.empty();
        return Registries.ITEM.getOrEmpty(Identifier.of(portalKey));
    }
    public boolean isPortalKeyBlank() {
        return getPortalKeyAsItem().isEmpty();
    }

    public NbtCompound notRandomTree(String tree, String block) {
        return CommonIO.readCarefully(InfinityMod.utilPath + "/placements/tree_vanilla.json", tree, block);
    }

    void saveTrees() {
        WeighedStructure<NbtElement> treesReg = compoundRegistry.get("trees");
        if (treesReg == null) return;
        List<String> trees = treesReg.keys.stream().map(compound -> ((NbtCompound)compound).getString("Name")).toList();
        double size = trees.size();
        NbtCompound c = new NbtCompound();
        NbtList l = new NbtList();
        c.put("default", notRandomTree(trees.getFirst(), "minecraft:grass_block"));
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
        pack.putInt("pack_format", 34);
        pack.putString("description", "Common template pools for Infinite Dimensions");
        res.put("pack", pack);
        CommonIO.write(res, savingPath, "pack.mcmeta");
    }

    public boolean roll(Random random, String key) {
        return (random.nextDouble() < rootChances.getOrDefault(key, 0.0));
    }
    public boolean rule(String key) {
        return gameRules.getOrDefault(key, false);
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
        boolean isRotatable = Registries.BLOCK.get(Identifier.of(block.getString("Name"))).getDefaultState().getProperties().contains(Properties.AXIS);
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

    public static Map<String, String> defaultMap = Map.ofEntries(
            Map.entry("all_blocks", "minecraft:stone"),
            Map.entry("top_blocks", "minecraft:stone"),
            Map.entry("blocks_features", "minecraft:stone"),
            Map.entry("full_blocks", "minecraft:stone"),
            Map.entry("full_blocks_worldgen", "minecraft:stone"),
            Map.entry("fluids", "minecraft:water"),
            Map.entry("items", "minecraft:stick"),
            Map.entry("sounds", "minecraft:block.stone.step"),
            Map.entry("music", "minecraft:music.game"),
            Map.entry("particles", "minecraft:heart"),
            Map.entry("biomes", "minecraft:plains"),
            Map.entry("mobs", "minecraft:pig"),
            Map.entry("tags", "#minecraft:air"),
            Map.entry("trees", "minecraft:oak"));

    public String randomName(Random random, String key) {
        return randomName(random, key, defaultMap.get(key));
    }

    public String randomName(Random random, String key, String def) {
        if (compoundRegistry.containsKey(key))
            return elementToName(compoundRegistry.get(key).getRandomElement(random));
        if (registry.containsKey(key))
            return registry.get(key).getRandomElement(random);
        return def;
    }

    public static String elementToName(NbtElement e) {
        if (e instanceof NbtCompound) return ((NbtCompound)e).getString("Name");
        else return e.asString();
    }

    public NbtCompound randomBlock(Random random, String key) {
        if (compoundRegistry.containsKey(key)) {
            NbtElement compound = compoundRegistry.get(key).getRandomElement(random);
            if (compound instanceof NbtCompound) return ((NbtCompound)compound);
            else return Block(compound.asString());
        }
        else {
            return Block(randomName(random, key));
        }
    }

    public NbtCompound randomFluid(Random random) {
        if (!compoundRegistry.containsKey("fluids")) return Fluid("minecraft:water");
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
                res.put("noise", CommonIO.read(InfinityMod.utilPath + "/noise.json"));
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

    static void addBounds(NbtCompound res, int lbound, int bound) {
        res.putInt("min_inclusive", lbound);
        res.putInt("max_inclusive", bound);
    }

    static void addBounds(NbtCompound res, Random random, int lbound, int bound) {
        int a = random.nextInt(lbound, bound);
        int b = random.nextInt(lbound, bound);
        addBounds(res, Math.min(a, b), Math.max(a, b));
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
                addBounds(res, random, lbound, bound);
                return res;
            }
            case 4 -> {
                res.putString("type", "clamped");
                addBounds(res, random, lbound, bound);
                res.put("source", intProvider(random, lbound, bound, false));
                return res;
            }
            case 3 -> {
                res.putString("type", "clamped_normal");
                addBounds(res, random, lbound, bound);
                res.putDouble("mean", lbound + random.nextDouble()*(bound-lbound));
                res.putDouble("deviation", random.nextExponential());
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
                int randomBound = max - min;
                if (randomBound <= 1) {
                    InfinityMod.LOGGER.debug("Corrected random bound of: {} to 2!", randomBound);
                    randomBound = 2;
                }   
                if (i==3 && random.nextBoolean()) res.putInt("plateau", random.nextInt(1, randomBound));
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
        float a = random.nextFloat(lbound, bound);
        float b = random.nextFloat(lbound, bound);
        float min = Math.min(a, b);
        float max = Math.max(a, b);
        switch (i) {
            case 0 -> {
                res.putFloat("max_exclusive", max);
                res.putFloat("min_inclusive", min);
            }
            case 1 -> {
                res.putFloat("max", max);
                res.putFloat("min", min);
                res.putFloat("mean", random.nextFloat(min, max));
                res.putFloat("deviation", random.nextFloat(max - min));
            }
            case 2 -> {
                res.putFloat("max", max);
                res.putFloat("min", min);
                res.putFloat("plateau", random.nextFloat(max - min));
            }
        }
        return res;
    }

    public void kickGhostsOut(DynamicRegistryManager s) {
        Registry<Biome> reg = s.get(RegistryKeys.BIOME);
        WeighedStructure<String> biomes = registry.get("biomes");
        if (biomes != null) {
            int i = 0;
            while(i < biomes.keys.size()) {
                if (!reg.containsId(Identifier.of(biomes.keys.get(i)))) {
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
}
