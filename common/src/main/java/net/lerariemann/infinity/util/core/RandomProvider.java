package net.lerariemann.infinity.util.core;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.item.Item;
import net.minecraft.nbt.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;

import static net.lerariemann.infinity.InfinityMod.configPath;

public class RandomProvider {
    public Map<ConfigType, WeighedStructure> registry = new HashMap<>();
    private final Map<String, Double> rootChances = new HashMap<>();
    private final Map<String, Boolean> gameRules = new HashMap<>();
    private final Map<String, Integer> gameRulesInt = new HashMap<>();
    private final Map<String, Double> gameRulesDouble = new HashMap<>();
    public ArrayList<String> disabledDimensions = new ArrayList<>();
    public Path savingPath;
    private String portalKey;
    public String salt;
    public Easterizer easterizer;

    public RandomProvider() {
        registerAll();
        easterizer = new Easterizer();
    }

    public RandomProvider(Path savingPath) {
        this();
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

    public String randomName(Random random, ConfigType key) {
        return randomName(random.nextDouble(), key);
    }
    public String randomName(net.minecraft.util.math.random.Random random, ConfigType key) {
        return randomName(random.nextDouble(), key);
    }
    public String randomName(double d, ConfigType key) {
        if (registry.containsKey(key))
            return registry.get(key).getName(d);
        return key.getDef();
    }

    public NbtCompound randomElement(net.minecraft.util.math.random.Random random, ConfigType key) {
        return randomElement(random.nextDouble(), key);
    }
    public NbtCompound randomElement(Random random, ConfigType key) {
        return randomElement(random.nextDouble(), key);
    }
    public NbtCompound randomElement(double d, ConfigType key) {
        if (registry.containsKey(key)) {
            WeighedStructure ws = registry.get(key);
            return ws.getElement(ws.getName(d), key.getConverter());
        }
        return key.fromName(key.getDef());
    }

    void registerAll() {
        readRootConfig();
        registerHardcoded();
        for (ConfigType type: ConfigType.normalModular) registerCategory(type);
        extractBlocks();
        extractMobs();
    }
    void registerCategory(ConfigType type) {
        registerCategory(type, CommonIO.readCategory(type));
    }
    void registerCategory(ConfigType type, List<NbtCompound> data) {
        registry.put(type, new WeighedStructure.Recursor(data, type));
    }

    void readRootConfig() {
        NbtCompound rootConfig = CommonIO.read(configPath.resolve("infinity.json"));
        portalKey = NbtUtils.getString(rootConfig, "portalKey");
        salt = NbtUtils.getString(rootConfig, "salt");
        NbtCompound gameRules = NbtUtils.getCompound(rootConfig, "gameRules");
        for (String s: gameRules.getKeys()) {
            NbtElement elem = gameRules.get(s);
            if (elem!=null) {
                if (elem.getType() == NbtElement.INT_TYPE) gameRulesInt.put(s, NbtUtils.getInt(gameRules, s));
                if (elem.getType() == NbtElement.DOUBLE_TYPE) gameRulesDouble.put(s, NbtUtils.getDouble(gameRules, s));
                else this.gameRules.put(s, gameRules.getBoolean(s));
            }
        }
        NbtCompound rootChances = NbtUtils.getCompound(rootConfig, "rootChances");
        for (String c: rootChances.getKeys()) {
            var compound = NbtUtils.getCompound(rootChances, c);
            for (String s: compound.getKeys()) {
                this.rootChances.put(s, NbtUtils.getDouble(compound, s));
            }
        }

        NbtList disabledDimensions = rootConfig.getList("disabledDimensions", NbtElement.STRING_TYPE);
        for (NbtElement jsonElement : disabledDimensions) {
            this.disabledDimensions.add(jsonElement.asString());
        }
    }

    void extractBlocks() {
        List<NbtCompound> blocksSettings = CommonIO.readCategory(ConfigType.BLOCKS);
        List<NbtCompound> allBlocks = new ArrayList<>();
        List<NbtCompound> fullBlocks = new ArrayList<>();
        List<NbtCompound> topBlocks = new ArrayList<>();
        List<NbtCompound> blocksFeatures = new ArrayList<>();
        List<NbtCompound> fullBlocksWG = new ArrayList<>();
        for (NbtCompound block : blocksSettings) {
            NbtCompound data = NbtUtils.getCompound(block, "data", new NbtCompound());
            boolean isfull, istop, isfloat, islaggy;
            isfull = popBlockData(data, "full", false);
            islaggy = popBlockData(data, "laggy", false);
            isfloat = popBlockData(data, "float", isfull);
            istop = popBlockData(data, "top", isfull);
            istop = istop || isfloat;
            block.put("data", data);
            allBlocks.add(block);
            if (isfull) fullBlocks.add(block);
            if (istop && !islaggy) topBlocks.add(block);
            if (isfloat) blocksFeatures.add(block);
            if (isfull && isfloat && !islaggy) fullBlocksWG.add(block);
        }
        registerCategory(ConfigType.ALL_BLOCKS, allBlocks);
        registerCategory(ConfigType.BLOCKS_FEATURES, blocksFeatures);
        registerCategory(ConfigType.FULL_BLOCKS, fullBlocks);
        registerCategory(ConfigType.FULL_BLOCKS_WG, fullBlocksWG);
        registerCategory(ConfigType.TOP_BLOCKS, topBlocks);
    }
    static boolean popBlockData(NbtCompound e, String key, boolean def) {
        boolean res = NbtUtils.test(e, key, def);
        e.remove(key);
        return res;
    }

    void extractMobs() {
        List<NbtCompound> allmobs = CommonIO.readCategory(ConfigType.MOBS);
        Map<ConfigType, List<NbtCompound>> byCategory = new HashMap<>();
        List<NbtCompound> cleanMobs = new ArrayList<>();
        for (ConfigType type : ConfigType.mobCategories) byCategory.put(type, new ArrayList<>());
        for (NbtCompound mob : allmobs) {
            String group = NbtUtils.getString(NbtUtils.getCompound(mob, "data"), "Category");
            mob.remove("data");
            ConfigType type = ConfigType.byName(group);
            if (type != null) {
                byCategory.get(type).add(mob);
                cleanMobs.add(mob);
            }
        }
        registerCategory(ConfigType.MOBS, cleanMobs);
        for (ConfigType type : byCategory.keySet())
            registerCategory(type, byCategory.get(type));
    }

    void registerHardcoded() {
        Path root = configPath.resolve("hardcoded");
        registerHardcoded(ConfigType.hardcoded, root);
        registerHardcoded(ConfigType.vegetation, root.resolve("vegetation"));
        registerHardcoded(ConfigType.features, root.resolve("features"));
    }

    void registerHardcoded(ConfigType[] types, Path dir) {
        for (ConfigType type: types) {
            Path file = dir.resolve(type.getKey() + ".json");
            if (file.toFile().exists()) {
                NbtCompound base = CommonIO.read(file);
                List<NbtCompound> list = base.getList("elements", NbtElement.COMPOUND_TYPE).stream().map(e -> (NbtCompound)e).toList();
                registry.put(type, new WeighedStructure.Simple(list, type.getDef()));
            }
        }
    }

    public List<String> getMobCategories() {
        return Arrays.stream(ConfigType.mobCategories).map(ConfigType::getKey).toList();
    }

    public NbtCompound blockToProvider(NbtCompound block, Random random) {
        NbtCompound res = new NbtCompound();
        boolean isRotatable = Registries.BLOCK.get(Identifier.of(NbtUtils.getString(block, "Name"))).getDefaultState().getProperties().contains(Properties.AXIS);
        res.putString("type", isRotatable && roll(random, "rotate_blocks") ?
                "minecraft:rotated_block_provider" : "minecraft:simple_state_provider");
        res.put("state", block);
        return res;
    }
    public NbtCompound randomBlockProvider(Random random, ConfigType key) {
        return blockToProvider(randomElement(random, key), random);
    }

    public NbtCompound randomPreset(Random random, String key) {
        String preset = randomName(random, ConfigType.COLOR_PRESETS);
        NbtList lst = new NbtList();
        Arrays.stream(ColorLogic.vanillaColors).forEach(color -> lst.add(NbtUtils.nameToElement(preset.replace("$", color))));
        NbtCompound res = new NbtCompound();
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
        return res;
    }

    public void kickGhostsOut(DynamicRegistryManager s) {
        registry.remove(ConfigType.BIOMES);
        List<NbtCompound> biomes = CommonIO.readCategory(ConfigType.BIOMES);
        Registry<Biome> reg = s.get(RegistryKeys.BIOME);
        registerCategory(ConfigType.BIOMES, biomes.stream().filter(comp -> reg.containsId(Identifier.of(NbtUtils.getString(comp,"Name")))).toList());
    }
}
