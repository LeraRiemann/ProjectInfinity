package net.lerariemann.infinity.util;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.structure.Structure;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import static net.lerariemann.infinity.util.ConfigManager.getConfigDir;

public interface ConfigGenerator {
    static <T> NbtCompound wsToCompound(WeighedStructure<T> w) {
        NbtCompound res = new NbtCompound();
        NbtList elements = new NbtList();
        int cse = 0;
        if (w.keys.getFirst() instanceof String) cse = 1;
        if (w.keys.getFirst() instanceof NbtCompound) cse = 2;
        if (w.keys.getFirst() instanceof NbtList) cse = 3;
        int finalCse = cse;
        List<Integer> range = new ArrayList<>(IntStream.rangeClosed(0, w.keys.size() - 1).boxed().toList());
        range.sort(new Comparator<Integer>() {
            public String extract(int i) {
                return switch (finalCse) {
                    case 2 -> ((NbtCompound)(w.keys.get(i))).getString("Name");
                    case 3 -> ((NbtList)(w.keys.get(i))).getFirst().toString();
                    default -> w.keys.get(i).toString();
                };
            }
            @Override
            public int compare(Integer i, Integer j) {
                return extract(i).compareTo(extract(j));
            }
        });
        for (int i = 0; i < w.keys.size(); i++) {
            NbtCompound element = new NbtCompound();
            T obj = w.keys.get(range.get(i));
            switch (cse) {
                case 1 -> element.putString("key", (String)obj);
                case 2 -> element.put("key", (NbtCompound)obj);
                case 3 -> element.put("key", (NbtList)obj);
            }
            element.putDouble("weight", w.weights.get(range.get(i)));
            elements.add(element);
        }
        res.put("elements", elements);
        return res;
    }

    static <T> void checkAndAddWS(Map<String, WeighedStructure<T>> m, String key) {
        if (!m.containsKey(key)) m.put(key, new WeighedStructure<>());
    }

    static void checkAndAddElement(Map<String, WeighedStructure<String>> m, Identifier id) {
        checkAndAddElement(m, id.getNamespace(), id.toString());
    }

    static <T> void checkAndAddElement(Map<String, WeighedStructure<T>> m, String namespace, T elem) {
        checkAndAddWS(m, namespace);
        m.get(namespace).add(elem, 1.0);
    }

    static <T> void writeMap(Map<String, WeighedStructure<T>> m, String addpath, String name) {
        m.keySet().forEach(key -> {
            if (!m.get(key).keys.isEmpty()) {
                CommonIO.write(wsToCompound(m.get(key)), getConfigDir()+ "/modular/" + key + "/" + addpath, name + ".json");
            }
        });
    }

    static <T> void writeMap(Map<String, WeighedStructure<T>> m, String addpath, String name, AtomicInteger i) {
        writeMap(m, addpath, name);
        info(i.get(), name);
    }

    static void info(int count, String type) {
        InfinityMod.LOGGER.info("Registered {} {}", count, type);
    }

    static <S> void generate(Registry<S> r, String additionalPath, String name) {
        generate(r, additionalPath, name, key -> key.getValue().toString());
    }

    static <S, T> void generate(Registry<S> reg, String additionalPath, String name, BiFunction<Registry<S>, RegistryKey<S>, T> extorter) {
        generate(reg, additionalPath, name, key -> extorter.apply(reg, key));
    }

    static <S, T> void generate(Registry<S> reg, String additionalPath, String name, Function<RegistryKey<S>, T> extorter) {
        Map<String, WeighedStructure<T>> map = new HashMap<>();
        AtomicInteger i = new AtomicInteger();
        reg.getKeys().forEach(key -> {
            T entry = extorter.apply(key);
            if (entry != null) {
                checkAndAddElement(map, key.getValue().getNamespace(), entry);
                i.getAndIncrement();
            }
        });
        writeMap(map, additionalPath, name, i);
    }

    static void generateSounds() {
        Registry<SoundEvent> r = Registries.SOUND_EVENT;
        Map<String, WeighedStructure<String>> music = new HashMap<>();
        Map<String, WeighedStructure<String>> sounds = new HashMap<>();
        AtomicInteger i = new AtomicInteger();
        AtomicInteger j = new AtomicInteger();
        r.getKeys().forEach(a -> {
            Identifier id = a.getValue();
            if (id.toString().contains("music")) {
                checkAndAddElement(music, id);
                i.getAndIncrement();
            }
            else {
                checkAndAddElement(sounds, id);
                j.getAndIncrement();
            }
        });
        writeMap(sounds, "misc", "sounds", j);
        writeMap(music, "misc", "music");
        info(i.get(), "music tracks");
    }

    static void generateBlockTags() {
        Map<String, WeighedStructure<String>> tagMap = new HashMap<>();
        AtomicInteger i = new AtomicInteger();
        Registries.BLOCK.streamTags().forEach(tagKey -> {
            checkAndAddWS(tagMap, tagKey.id().getNamespace());
            tagMap.get(tagKey.id().getNamespace()).add("#" + tagKey.id().toString(), 1.0);
            i.getAndIncrement();
        });
        writeMap(tagMap, "misc", "tags");
        info(i.get(), "block tags");
    }

    static Set<String> generateFluids() {
        Map<String, WeighedStructure<NbtCompound>> fluidMap = new HashMap<>();
        Registry<Fluid> r = Registries.FLUID;
        AtomicInteger i = new AtomicInteger();
        Set<String> blocknames = new HashSet<>();
        r.getKeys().forEach(a -> {
            NbtCompound data = extractFluid(a);
            blocknames.add(data.getString("Name"));
            Fluid f = r.get(a.getValue());
            if (f instanceof FlowableFluid fl) {
                String namespace = a.getValue().getNamespace();
                checkAndAddWS(fluidMap, namespace);
                if (fl.equals(fl.getStill())) {
                    fluidMap.get(namespace).add(data, 1.0);
                    i.getAndIncrement();
                }
            }
        });
        writeMap(fluidMap, "blocks", "fluids", i);
        return blocknames;
    }

    static void generateBlocksAndFluids(WorldView w, BlockPos inAir, BlockPos onStone) {
        Map<String, WeighedStructure<NbtCompound>> blockMap = new HashMap<>();
        Map<String, WeighedStructure<NbtList>> colorPresetMap = new HashMap<>();
        Map<String, WeighedStructure<String>> airMap = new HashMap<>();
        Map<String, WeighedStructure<String>> flowerMap = new HashMap<>();
        Registry<Block> r = Registries.BLOCK;
        Set<String> fluidBlockNames = generateFluids();
        AtomicInteger i = new AtomicInteger();
        r.getKeys().forEach(key -> {
            String blockName = key.getValue().toString();
            if(!fluidBlockNames.contains(blockName)) {
                Block block = r.get(key);
                assert block != null;
                String namespace = key.getValue().getNamespace();
                checkAndAddWS(blockMap, namespace);
                checkAndAddWS(colorPresetMap, namespace);
                blockMap.get(namespace).add(extractBlock(key, w, inAir, onStone), 1.0);
                if (blockName.contains("magenta") && !isLaggy(block) && isFloat(block.getDefaultState(), w, inAir))
                    checkColorSet(blockName, colorPresetMap.get(namespace));
                if (block.getDefaultState().isIn(BlockTags.AIR)) checkAndAddElement(airMap, key.getValue());
                if (block.getDefaultState().isIn(BlockTags.SMALL_FLOWERS)) checkAndAddElement(flowerMap, key.getValue());
                i.getAndIncrement();
            }
        });
        writeMap(blockMap, "blocks", "blocks", i);
        writeMap(colorPresetMap, "extra", "color_presets");
        writeMap(airMap, "blocks", "airs");
        writeMap(flowerMap, "blocks", "flowers");
    }

    static void checkColorSet(String block, WeighedStructure<NbtList> w) {
        String[] colors = {"white", "light_gray", "gray", "black", "brown", "red", "orange", "yellow", "lime", "green",
                "light_blue", "blue", "cyan", "purple", "magenta", "pink"};
        AtomicInteger successCounter = new AtomicInteger();
        NbtList colorSet = new NbtList();
        Arrays.stream(colors).forEach(color -> {
            int i = block.lastIndexOf("magenta");
            String blockColored = block.substring(0, i) + color + block.substring(i+7);
            if (Registries.BLOCK.containsId(Identifier.of(blockColored))) {
                successCounter.addAndGet(1);
                NbtCompound c = new NbtCompound();
                c.putString("Name", blockColored);
                colorSet.add(c);
            }
        });
        if (successCounter.get() == colors.length) w.add(colorSet, 1.0);
    }

    static NbtCompound extractFluid(RegistryKey<Fluid> key) {
        Fluid b = Registries.FLUID.get(key);
        assert b!= null;
        NbtCompound res = new NbtCompound();
        String name = Registries.BLOCK.getId(b.getDefaultState().getBlockState().getBlock()).toString();
        res.putString("Name", name);
        res.putString("fluidName", key.getValue().toString());
        return res;
    }

    static NbtCompound extractBlock(RegistryKey<Block> key, WorldView w, BlockPos inAir, BlockPos onStone) {
        Block b = Registries.BLOCK.get(key);
        assert b!= null;
        BlockState bs = b.getDefaultState();
        NbtCompound res = new NbtCompound();
        res.putString("Name", key.getValue().toString());
        res.putBoolean("laggy", isLaggy(b));
        res.putBoolean("full", isFull(bs, w, inAir));
        res.putBoolean("float", isFloat(bs, w, inAir));
        NbtCompound properties = new NbtCompound();
        if (bs.contains(Properties.PERSISTENT)) properties.putString("persistent", "true");
        if (bs.contains(Properties.LIT)) properties.putString("lit", "false");
        if (bs.contains(Properties.BLOCK_FACE)) {
            properties.putString("face", "floor");
            bs = bs.with(Properties.BLOCK_FACE, BlockFace.FLOOR);
        }
        res.putBoolean("top", isTop(bs, w, onStone));
        if (!properties.isEmpty()) res.put("Properties", properties);
        return res;
    }

    static boolean isLaggy(Block b) {
        return (b.getDefaultState().hasBlockEntity());
    }

    static boolean isTop(BlockState bs, WorldView w, BlockPos onStone) {
        try {
            return bs.canPlaceAt(w, onStone);
        }
        catch (Exception e) {
            return false;
        }
    }
    static boolean isFloat(BlockState bs, WorldView w, BlockPos inAir) {
        try {
            return bs.canPlaceAt(w, inAir) && !(bs.getBlock() instanceof FallingBlock);
        }
        catch (Exception e) {
            return false;
        }
    }
    static boolean isFull(BlockState bs, WorldView w, BlockPos inAir) {
        return bs.isFullCube(w, inAir);
    }

    static String extractParticle(RegistryKey<ParticleType<?>> key) {
        Identifier id = key.getValue();
        if (!id.getNamespace().equals("minecraft") && !(Registries.PARTICLE_TYPE.get(id) instanceof SimpleParticleType)) return null;
        return id.toString();
    }

    static NbtCompound extractMob(RegistryKey<EntityType<?>> key) {
        if (key.getValue().getNamespace().equals("minecolonies")) return null; //this mod's mobs crash the game when spawned from the biome
        SpawnGroup sg = Registries.ENTITY_TYPE.get(key.getValue()).getSpawnGroup();
        if (sg == SpawnGroup.MISC) return null; //minecarts n stuff
        NbtCompound mob = new NbtCompound();
        mob.putString("Name", key.getValue().toString());
        mob.putString("Category", sg.asString());
        return mob;
    }

    static NbtCompound extractEffect(RegistryKey<StatusEffect> key) {
        NbtCompound res = new NbtCompound();
        StatusEffectCategory cat = Objects.requireNonNull(Registries.STATUS_EFFECT.get(key)).getCategory();
        res.putString("Name", key.getValue().toString());
        res.putString("Category", switch (cat) { //this data is unused for now but might be important later
            case HARMFUL -> "harmful";
            case BENEFICIAL -> "beneficial";
            case NEUTRAL -> "neutral";
        });
        return res;
    }

    static NbtCompound extractStructure(Registry<Structure> registry, RegistryKey<Structure> key) {
        Identifier id = key.getValue();
        if (id.getNamespace().equals("infinity") && id.getPath().contains("_") && !id.getPath().equals("indev_house")) return null;
        Optional<Structure> o = registry.getOrEmpty(key);
        if (o.isEmpty()) return null;
        Structure structure = o.get();
        String step = structure.getFeatureGenerationStep().name().toLowerCase();
        String adaptation = structure.getTerrainAdaptation().name().toLowerCase();
        NbtCompound overrides = genOverrides(structure.getStructureSpawns());
        NbtCompound res = new NbtCompound();
        res.putString("id", id.toString());
        res.putString("step", step);
        res.put("spawn_overrides", overrides);
        res.putString("terrain_adaptation", adaptation);
        return res;
    }

    static NbtCompound genOverrides(Map<SpawnGroup, StructureSpawns> overrides) {
        NbtCompound res = new NbtCompound();
        overrides.forEach((key, value) -> res.put(key.name().toLowerCase(), genOverride(value)));
        return res;
    }

    static NbtCompound genOverride(StructureSpawns spawns) {
        NbtCompound res = new NbtCompound();
        StructureSpawns.BoundingBox box = spawns.boundingBox();
        res.putString("bounding_box", box == StructureSpawns.BoundingBox.PIECE ? "piece" : "full");
        res.put("spawns", genSpawns(spawns.spawns()));
        return res;
    }

    static NbtList genSpawns(Pool<SpawnSettings.SpawnEntry> entries) {
        NbtList lst = new NbtList();
        entries.getEntries().forEach((entry) -> lst.add(genEntry(entry)));
        return lst;
    }

    static NbtCompound genEntry(SpawnSettings.SpawnEntry entry) {
        NbtCompound res = new NbtCompound();
        res.putString("type", Registries.ENTITY_TYPE.getId(entry.type).toString());
        res.putInt("maxCount", entry.maxGroupSize);
        res.putInt("minCount", entry.minGroupSize);
        res.putInt("weight", entry.getWeight().getValue());
        return res;
    }

    static NbtCompound extractFeature(Registry<ConfiguredFeature<?,?>> registry, RegistryKey<ConfiguredFeature<?,?>> key) {
        Identifier id = key.getValue();
        if (id.getNamespace().equals("infinity") || id.toString().contains("bees")) return null;
        Optional<ConfiguredFeature<?,? extends Feature<?>>> o = registry.getOrEmpty(key);
        if (o.isEmpty()) return null;
        ConfiguredFeature<?,? extends Feature<?>> feature = o.get();
        String type = getFeatureType(feature.feature());
        if (type.isEmpty()) return null;
        NbtCompound res = new NbtCompound();
        res.putString("Name", id.toString());
        res.putString("Type", type); //this data is unused for now but might be important later
        return res;
    }

    static String getFeatureType(Feature<?> type) {
        if (type.equals(Feature.TREE)) return "tree";
        if (type.equals(Feature.HUGE_FUNGUS)) return "huge_fungus";
        if (type.equals(Feature.HUGE_BROWN_MUSHROOM)) return "huge_brown_mushroom";
        if (type.equals(Feature.HUGE_RED_MUSHROOM)) return "huge_red_mushroom";
        return "";
    }

    static void generateAll(World w, BlockPos inAir, BlockPos onStone) {
        MinecraftServer s = Objects.requireNonNull(w.getServer());
        DynamicRegistryManager manager = s.getRegistryManager();
        generateAllNoWorld();
        generateBlocksAndFluids(w, inAir, onStone);
        SurfaceRuleScanner.scan(s);
        generate(manager.get(RegistryKeys.BIOME), "misc", "biomes", key ->
                key.getValue().getNamespace().equals(InfinityMod.MOD_ID) ? null : key.getValue().toString());
        generate(manager.get(RegistryKeys.STRUCTURE), "extra", "structures", ConfigGenerator::extractStructure);
        generate(manager.get(RegistryKeys.CONFIGURED_FEATURE), "extra", "trees", ConfigGenerator::extractFeature);
    }

    static void generateAllNoWorld() {
        generateSounds();
        generate(Registries.ITEM, "misc", "items");
        generate(Registries.PARTICLE_TYPE, "misc", "particles", ConfigGenerator::extractParticle);
        generate(Registries.ENTITY_TYPE, "extra", "mobs", ConfigGenerator::extractMob);
        generateBlockTags();
        generate(Registries.STATUS_EFFECT, "extra", "effects", ConfigGenerator::extractEffect);
    }
}
