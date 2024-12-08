package net.lerariemann.infinity.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
import net.minecraft.world.gen.structure.Structure;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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

    static NbtCompound block(RegistryKey<Block> key, WorldView w, BlockPos inAir, BlockPos onStone) {
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

    static NbtCompound fluid(RegistryKey<Fluid> key) {
        Fluid b = Registries.FLUID.get(key);
        assert b!= null;
        NbtCompound res = new NbtCompound();
        res.putString("Name", Registries.BLOCK.getId(b.getDefaultState().getBlockState().getBlock()).toString());
        res.putString("fluidName", key.getValue().toString());
        return res;
    }

    static <T> void checkAndAddWS(Map<String, WeighedStructure<T>> m, String key) {
        if (!m.containsKey(key)) m.put(key, new WeighedStructure<>());
    }

    static void checkAndAddElement(Map<String, WeighedStructure<String>> m, Identifier id) {
        checkAndAddWS(m, id.getNamespace());
        m.get(id.getNamespace()).add(id.toString(), 1.0);
    }

    static <T extends NbtElement> void checkAndAddElement(Map<String, WeighedStructure<T>> m, String namespace, T elem) {
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

    static <T> void generate(Registry<T> r, String additionalPath, String name) {
        generate(r, additionalPath, name, false);
    }

    static <T> void generate(Registry<T> r, String additionalPath, String name, boolean excludeInfinity) {
        Map<String, WeighedStructure<String>> m = new HashMap<>();
        r.getKeys().forEach(key -> {
            String namespace = key.getValue().getNamespace();
            if (!excludeInfinity || !(namespace.contains("infinity"))) {
                checkAndAddElement(m, key.getValue());
            }
        });
        writeMap(m, additionalPath, name);
    }

    static void generateParticles() {
        Registry<ParticleType<?>> r = Registries.PARTICLE_TYPE;
        Map<String, WeighedStructure<String>> m = new HashMap<>();
        r.getKeys().forEach(a -> {
            Identifier id = a.getValue();
            if (id.getNamespace().equals("minecraft") || r.get(id) instanceof SimpleParticleType) {
                checkAndAddElement(m, id);
            }
        });
        writeMap(m, "misc", "particles");
    }

    static void generateBlockTags() {
        Map<String, WeighedStructure<String>> tagMap = new HashMap<>();
        Registries.BLOCK.streamTags().forEach(tagKey -> {
            checkAndAddWS(tagMap, tagKey.id().getNamespace());
            tagMap.get(tagKey.id().getNamespace()).add("#" + tagKey.id().toString(), 1.0);
        });
        writeMap(tagMap, "misc", "tags");
    }

    static void generateFluids() {
        Registry<Fluid> r = Registries.FLUID;
        Map<String, WeighedStructure<NbtCompound>> m = new HashMap<>();
        r.getKeys().forEach(a -> {
            String b = a.getValue().toString();
            String namespace = a.getValue().getNamespace();
            checkAndAddWS(m, namespace);
            if (!b.contains("flowing"))
                m.get(namespace).add(fluid(a), 1.0);
        });
        writeMap(m, "blocks", "fluids");
    }

    static void generateMobs() {
        Map<String, WeighedStructure<NbtCompound>> allMobs = new HashMap<>();
        Registries.ENTITY_TYPE.getKeys().forEach(key -> {
            NbtCompound mob = mob(key);
            if (mob != null) {
                checkAndAddElement(allMobs, key.getValue().getNamespace(), mob);
            }
        });
        writeMap(allMobs, "extra", "mobs");
    }

    static NbtCompound mob(RegistryKey<EntityType<?>> key) {
        SpawnGroup sg = Registries.ENTITY_TYPE.get(key.getValue()).getSpawnGroup();
        if (sg != SpawnGroup.MISC) {
            NbtCompound mob = new NbtCompound();
            mob.putString("Name", key.getValue().toString());
            mob.putString("Category", sg.asString());
            return mob;
        }
        return null;
    }

    static void generateEffects() {
        Map<String, WeighedStructure<NbtCompound>> allEffects = new HashMap<>();
        Registries.STATUS_EFFECT.getKeys().forEach(key -> checkAndAddElement(allEffects, key.getValue().getNamespace(), effect(key)));
        writeMap(allEffects, "extra", "effects");
    }

    static NbtCompound effect(RegistryKey<StatusEffect> key) {
        NbtCompound res = new NbtCompound();
        StatusEffectCategory cat = Objects.requireNonNull(Registries.STATUS_EFFECT.get(key)).getCategory();
        res.putString("Name", key.getValue().toString());
        res.putString("Category", switch (cat) {
            case HARMFUL -> "harmful";
            case BENEFICIAL -> "beneficial";
            case NEUTRAL -> "neutral";
        });
        return res;
    }

    static void generateBlocks(WorldView w, BlockPos inAir, BlockPos onStone) {
        Registry<Block> r = Registries.BLOCK;
        Map<String, WeighedStructure<NbtCompound>> blockMap = new HashMap<>();
        Map<String, WeighedStructure<NbtList>> colorPresetMap = new HashMap<>();
        Map<String, WeighedStructure<String>> airMap = new HashMap<>();
        Map<String, WeighedStructure<String>> flowerMap = new HashMap<>();
        r.getKeys().forEach(key -> {
            Block block = r.get(key);
            assert block != null;
            if(block.getDefaultState().getFluidState().isOf(Fluids.EMPTY)) {
                String blockName = key.getValue().toString();
                String namespace = key.getValue().getNamespace();
                checkAndAddWS(blockMap, namespace);
                checkAndAddWS(colorPresetMap, namespace);
                blockMap.get(namespace).add(block(key, w, inAir, onStone), 1.0);
                if (blockName.contains("magenta") && !isLaggy(block) && isFloat(block.getDefaultState(), w, inAir))
                    checkColorSet(blockName, colorPresetMap.get(namespace));
                if (block.getDefaultState().isIn(BlockTags.AIR)) checkAndAddElement(airMap, key.getValue());
                if (block.getDefaultState().isIn(BlockTags.SMALL_FLOWERS)) checkAndAddElement(flowerMap, key.getValue());
            }
        });
        writeMap(blockMap, "blocks", "blocks");
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

    static void generateSounds() {
        Registry<SoundEvent> r = Registries.SOUND_EVENT;
        Map<String, WeighedStructure<String>> music = new HashMap<>();
        Map<String, WeighedStructure<String>> sounds = new HashMap<>();
        r.getKeys().forEach(a -> {
            Identifier id = a.getValue();
            if (id.toString().contains("music")) {
                checkAndAddElement(music, id);
            }
            else {
                checkAndAddElement(sounds, id);
            }
        });
        writeMap(sounds, "misc", "sounds");
        writeMap(music, "misc", "music");
    }

    static void generateStructures(MinecraftServer server) {
        Map<String, WeighedStructure<NbtCompound>> map = new HashMap<>();
        Registry<Structure> registry = server.getRegistryManager().get(RegistryKeys.STRUCTURE);
        registry.getKeys().forEach(key -> {
            Identifier id = key.getValue();
            if (!id.getNamespace().equals("infinity") || !id.getPath().contains("_") || id.getPath().equals("indev_house")) {
                Optional<Structure> o = registry.getOrEmpty(key);
                o.ifPresent(structure -> {
                    String step = structure.getFeatureGenerationStep().name().toLowerCase();
                    String adaptation = structure.getTerrainAdaptation().name().toLowerCase();
                    NbtCompound overrides = genOverrides(structure.getStructureSpawns());
                    NbtCompound res = new NbtCompound();
                    res.putString("id", id.toString());
                    res.putString("step", step);
                    res.put("spawn_overrides", overrides);
                    res.putString("terrain_adaptation", adaptation);
                    checkAndAddElement(map, id.getNamespace(), res);
                });
            }
        });
        writeMap(map, "extra", "structures");
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

    static void generateAll(World w, BlockPos inAir, BlockPos onStone) {
        generateAllNoWorld();
        generateBlocks(w, inAir, onStone);
        MinecraftServer s = Objects.requireNonNull(w.getServer());
        SurfaceRuleScanner.scan(s);
        generate(s.getRegistryManager().get(RegistryKeys.BIOME), "misc", "biomes", true);
        generateStructures(s);
    }

    static void generateAllNoWorld() {
        generateSounds();
        generate(Registries.ITEM, "misc", "items");
        generateParticles();
        generateMobs();
        generateFluids();
        generateBlockTags();
        generateEffects();
    }
}
