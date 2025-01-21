package net.lerariemann.infinity.util.core;

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
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.nio.file.Files.walk;
import static net.lerariemann.infinity.InfinityMod.configPath;

public class RandomProvider {
    public Map<String, WeighedStructure<String>> registry = new HashMap<>();
    public Map<String, WeighedStructure<NbtElement>> compoundRegistry = new HashMap<>();
    private final Map<String, Double> rootChances = new HashMap<>();
    private final Map<String, Boolean> gameRules = new HashMap<>();
    private final Map<String, Integer> gameRulesInt = new HashMap<>();
    private final Map<String, Double> gameRulesDouble = new HashMap<>();
    public ArrayList<String> disabledDimensions = new ArrayList<>();
    public Path savingPath;
    private String portalKey;
    public String salt;
    public Easterizer easterizer;

    public RandomProvider(Path savingPath) {
        registerAll();
        easterizer = new Easterizer();
        this.savingPath = savingPath;
        (new CorePack(this, savingPath)).generate();
    }

    public Optional<Item> getPortalKeyAsItem() {
        if (portalKey.isBlank()) return Optional.empty();
        return Registries.ITEM.getOrEmpty(Identifier.of(portalKey));
    }
    public boolean isPortalKeyBlank() {
        return getPortalKeyAsItem().isEmpty();
    }
    public boolean roll(Random random, String key) {
        return (random.nextDouble() < rootChances.getOrDefault(key, 0.0));
    }

    private static <T> T getStaticRule(BiFunction<RandomProvider, String, T> applier,
                                      BiFunction<NbtCompound, String, T> applier2,
                                      String key, T def) {
        if (InfinityMod.provider != null) {
            return applier.apply(InfinityMod.provider, key);
        }
        Path root = configPath.resolve("infinity.json");
        if (!root.toFile().exists()) return def;
        NbtCompound rules = CommonIO.read(configPath.resolve("infinity.json")).getCompound("gameRules");
        if (!rules.contains(key)) return def;
        return applier2.apply(rules, key);
    }
    public static boolean rule(String key) {
        return getStaticRule((p, k) -> p.gameRules.getOrDefault(k, false), NbtCompound::getBoolean, key, false);
    }
    public static int ruleInt(String key) {
        return getStaticRule(RandomProvider::_ruleInt, (p, k) -> ((AbstractNbtNumber) Objects.requireNonNull(p.get(k))).intValue(), key, -1);
    }
    private int _ruleInt(String key) {
        if (gameRulesInt.containsKey(key)) return gameRulesInt.get(key);
        return (gameRulesDouble.get(key)).intValue();
    }

    public String randomName(Random random, String key) {
        return randomName(random.nextDouble(), key);
    }
    public String randomName(net.minecraft.util.math.random.Random random, String key) {
        return randomName(random.nextDouble(), key);
    }
    public String randomName(double d, String key) {
        if (compoundRegistry.containsKey(key))
            return NbtUtils.elementToName(compoundRegistry.get(key).getElement(d));
        if (registry.containsKey(key))
            return registry.get(key).getElement(d);
        return defaultMap.get(key);
    }

    public NbtCompound randomElement(net.minecraft.util.math.random.Random random, String key) {
        return randomElement(random.nextDouble(), key);
    }
    public NbtCompound randomElement(Random random, String key) {
        return randomElement(random.nextDouble(), key);
    }
    public NbtCompound randomElement(double d, String key) {
        return randomElementInternal(d, key, key.equals("fluids") ? NbtUtils::nameToFluid : NbtUtils::nameToElement);
    }
    private NbtCompound randomElementInternal(double d, String key, Function<String, NbtCompound> converter) {
        if (compoundRegistry.containsKey(key)) {
            NbtElement compound = compoundRegistry.get(key).getElement(d);
            if (compound instanceof NbtCompound) return ((NbtCompound)compound);
            else if (compound instanceof NbtString) return converter.apply(compound.asString());
        }
        else if (registry.containsKey(key))
            return converter.apply(registry.get(key).getElement(d));
        return converter.apply(defaultMap.get(key));
    }

    public static final Map<String, String> defaultMap = Map.ofEntries(
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
            Map.entry("tags", "minecraft:air"),
            Map.entry("trees", "minecraft:oak"),
            Map.entry("loot_tables", "minecraft:blocks/stone"));

    void registerAll() {
        readRootConfig();
        registerCategory(registry, "misc", CommonIO::readStringList);
        registerCategory(registry, "features", CommonIO::readStringList);
        registerCategory(registry, "vegetation", CommonIO::readStringList);
        registerCategory(compoundRegistry, "blocks", CommonIO::readCompoundList);
        extractBlocks();
        registerCategory(compoundRegistry, "extra", CommonIO::readCompoundList);
        extractMobs();
        registerHardcoded();
    }

    void readRootConfig() {
        NbtCompound rootConfig = CommonIO.read(configPath.resolve("infinity.json"));
        portalKey = rootConfig.getString("portalKey");
        salt = rootConfig.getString("salt");
        NbtCompound gamerules = rootConfig.getCompound("gameRules");
        for (String s: gamerules.getKeys()) {
            NbtElement elem = gamerules.get(s);
            if (elem!=null) {
                if (elem.getType() == NbtElement.INT_TYPE) gameRulesInt.put(s, gamerules.getInt(s));
                if (elem.getType() == NbtElement.DOUBLE_TYPE) gameRulesDouble.put(s, gamerules.getDouble(s));
                else gameRules.put(s, gamerules.getBoolean(s));
            }
        }
        NbtCompound rootchances = rootConfig.getCompound("rootChances");
        for (String c: rootchances.getKeys()) {
            for (String s: rootchances.getCompound(c).getKeys()) {
                rootChances.put(s, rootchances.getCompound(c).getDouble(s));
            }
        }

        NbtList disableddimensions = rootConfig.getList("disabledDimensions", 8);
        for (NbtElement jsonElement : disableddimensions) {
            disabledDimensions.add(jsonElement.asString());
        }
    }

    void extractBlocks() {
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
                isfull = popBlockData(e, "full", false);
                islaggy = popBlockData(e, "laggy", false);
                isfloat = popBlockData(e, "float", isfull);
                istop = popBlockData(e, "top", isfull);
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
    static boolean popBlockData(NbtCompound e, String key, boolean def) {
        boolean res = NbtUtils.test(e, key, def);
        e.remove(key);
        return res;
    }

    void extractMobs() {
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

    <B> void registerCategory(Map<String, B> reg, String subpath, ListReader<B> reader) {
        Path path = configPath.resolve("modular");
        try (Stream<Path> files = walk(path.resolve("minecraft").resolve(subpath))) {
            files.filter(p -> p.toFile().isFile()).forEach(p -> {
                String fullname = p.toString();
                String name = p.getFileName().toString().replace(".json", "");
                int i = fullname.replace("minecraft_", "%%%").lastIndexOf("minecraft");
                String sub = fullname.substring(i+10);
                reg.put(name, reader.op(path, sub));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void registerHardcoded() {
        try (Stream<Path> files = walk(configPath.resolve("hardcoded"))) {
            files.map(Path::toString).filter(s -> s.endsWith(".json")).forEach(fullname -> {
                String name = fullname.substring(fullname.lastIndexOf("/") + 1, fullname.length() - 5);
                name = name.substring(name.lastIndexOf('\\') + 1);
                if (!name.equals("none")) registry.put(name, CommonIO.readStringList(fullname));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getMobCategories() {
        return registry.get("mob_categories").keys;
    }

    public NbtCompound blockToProvider(NbtCompound block, Random random) {
        NbtCompound res = new NbtCompound();
        boolean isRotatable = Registries.BLOCK.get(Identifier.of(block.getString("Name"))).getDefaultState().getProperties().contains(Properties.AXIS);
        res.putString("type", isRotatable && roll(random, "rotate_blocks") ?
                "minecraft:rotated_block_provider" : "minecraft:simple_state_provider");
        res.put("state", block);
        return res;
    }
    public NbtCompound randomBlockProvider(Random random, String key) {
        return blockToProvider(randomElement(random, key), random);
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
        B op(Path path, String subpath);
    }
}
