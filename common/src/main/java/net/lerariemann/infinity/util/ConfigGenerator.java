package net.lerariemann.infinity.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static net.lerariemann.infinity.util.ConfigManager.getConfigDir;

public class ConfigGenerator {
    public static <T> NbtCompound wsToCompound(WeighedStructure<T> w) {
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

    public static boolean isLaggy(Block b) {
        return (b.getDefaultState().hasBlockEntity());
    }

    public static boolean isTop(BlockState bs, WorldView w, BlockPos onStone) {
        try {
            return bs.canPlaceAt(w, onStone);
        }
        catch (Exception e) {
            return false;
        }
    }
    public static boolean isFloat(BlockState bs, WorldView w, BlockPos inAir) {
        try {
            return bs.canPlaceAt(w, inAir) && !(bs.getBlock() instanceof FallingBlock);
        }
        catch (Exception e) {
            return false;
        }
    }
    public static boolean isFull(BlockState bs, WorldView w, BlockPos inAir) {
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

    public static <T> void checkAndAddWS(Map<String, WeighedStructure<T>> m, String key) {
        if (!m.containsKey(key)) m.put(key, new WeighedStructure<>());
    }

    public static <T> void generate(Registry<T> r, String additionalPath, String name) {
        generate(r, additionalPath, name, false);
    }

    public static <T> void generate(Registry<T> r, String additionalPath, String name, boolean excludeInfinity) {
        Map<String, WeighedStructure<String>> m = new HashMap<>();
        r.getKeys().forEach(a -> {
            String b = a.getValue().toString();
            String namespace = a.getValue().getNamespace();
            if (!excludeInfinity || !(namespace.contains("infinity"))) {
                checkAndAddWS(m, namespace);
                m.get(namespace).add(b, 1.0);
            }
        });
        writeMap(m, additionalPath, name);
    }

    public static void generateParticles() {
        Registry<ParticleType<?>> r = Registries.PARTICLE_TYPE;
        Map<String, WeighedStructure<String>> m = new HashMap<>();
        r.getKeys().forEach(a -> {
            String b = a.getValue().toString();
            String namespace = a.getValue().getNamespace();
            if (namespace.equals("minecraft") || r.get(a.getValue()) instanceof SimpleParticleType) {
                checkAndAddWS(m, namespace);
                m.get(namespace).add(b, 1.0);
            }
        });
        writeMap(m, "misc", "particles");
    }

    public static void generateFluids() {
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

    public static void generateMobs() {
        Registry<EntityType<?>> r = Registries.ENTITY_TYPE;
        Map<String, Map<String, WeighedStructure<String>>> m = new HashMap<>();
        Map<String, WeighedStructure<String>> allMobs = new HashMap<>();
        r.getKeys().forEach(a -> {
            String b = a.getValue().toString();
            String namespace = a.getValue().getNamespace();
            SpawnGroup sg = r.get(a.getValue()).getSpawnGroup();
            if (sg != SpawnGroup.MISC) {
                if (!m.containsKey(sg.asString())) m.put(sg.asString(), new HashMap<>());
                Map<String, WeighedStructure<String>> m2 = m.get(sg.asString());
                checkAndAddWS(m2, namespace);
                checkAndAddWS(allMobs, namespace);
                m2.get(namespace).add(b, 1.0);
                allMobs.get(namespace).add(b, 1.0);
            }
        });
        m.keySet().forEach(key -> writeMap(m.get(key), "mobs", key));
        writeMap(allMobs, "mobs", "mobs");
    }

    public static void generateBlocks(WorldView w, BlockPos inAir, BlockPos onStone) {
        Registry<Block> r = Registries.BLOCK;
        Map<String, WeighedStructure<NbtCompound>> m = new HashMap<>();
        Map<String, WeighedStructure<NbtList>> m2 = new HashMap<>();
        r.getKeys().forEach(a -> {
            Block block = r.get(a);
            assert block != null;
            if(block.getDefaultState().getFluidState().isOf(Fluids.EMPTY)) {
                String b = a.getValue().toString();
                String namespace = a.getValue().getNamespace();
                checkAndAddWS(m, namespace);
                checkAndAddWS(m2, namespace);
                m.get(namespace).add(block(a, w, inAir, onStone), 1.0);
                if (b.contains("magenta") && !isLaggy(block) && isFloat(block.getDefaultState(), w, inAir))
                    checkColorSet(b, m2.get(namespace));
            }
        });
        writeMap(m, "blocks", "blocks");
        writeMap(m2, "extra", "color_presets");
    }

    public static void checkColorSet(String block, WeighedStructure<NbtList> w) {
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

    public static <T> void writeMap(Map<String, WeighedStructure<T>> m, String addpath, String name) {
        m.keySet().forEach(a -> {
            if (!m.get(a).keys.isEmpty()) {
                try {
                    Files.createDirectories(Paths.get(getConfigDir()+ "/modular/" + a + "/" + addpath));
                    CommonIO.write(wsToCompound(m.get(a)), getConfigDir()+ "/modular/" + a + "/" + addpath, name + ".json");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static void generateSounds() {
        Registry<SoundEvent> r = Registries.SOUND_EVENT;
        Map<String, WeighedStructure<String>> music = new HashMap<>();
        Map<String, WeighedStructure<String>> sounds = new HashMap<>();
        r.getKeys().forEach(a -> {
            String b = a.getValue().toString();
            String namespace = a.getValue().getNamespace();
            if (b.contains("music")) {
                checkAndAddWS(music, namespace);
                music.get(namespace).add(b, 1.0);
            }
            else {
                checkAndAddWS(sounds, namespace);
                sounds.get(namespace).add(b, 1.0);
            }
        });
        writeMap(sounds, "misc", "sounds");
        writeMap(music, "misc", "music");
    }

    public static void generateAll(World w, BlockPos inAir, BlockPos onStone) {
        generateAllNoWorld();
        generateBlocks(w, inAir, onStone);
        MinecraftServer s = Objects.requireNonNull(w.getServer());
        SurfaceRuleScanner.scan(s);
        generate(s.getRegistryManager().getOrThrow(RegistryKeys.BIOME), "misc", "biomes", true);
    }

    public static void generateAllNoWorld() {
        generateSounds();
        generate(Registries.ITEM, "misc", "items");
        generateParticles();
        generateMobs();
        generateFluids();
    }
}
