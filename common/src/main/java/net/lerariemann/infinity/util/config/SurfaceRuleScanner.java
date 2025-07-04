package net.lerariemann.infinity.util.config;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.dimensions.RandomNoisePreset;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface SurfaceRuleScanner {
    static void scan(MinecraftServer server) {
        Map<String, NbtCompound> map = new HashMap<>();
        Registry<ChunkGeneratorSettings> registry = server.getRegistryManager().get(RegistryKeys.CHUNK_GENERATOR_SETTINGS);
        registry.getKeys().forEach(key -> {
            if (!key.getValue().getNamespace().contains("infinity")) {
                Optional<ChunkGeneratorSettings> o = registry.getOrEmpty(key);
                o.ifPresent(settings -> {
                    Optional<NbtElement> c = ChunkGeneratorSettings.CODEC.encodeStart(NbtOps.INSTANCE, settings).result();
                    c.ifPresent(e -> {
                        Tree t = new Tree(NbtUtils.getCompound(((NbtCompound) e), "surface_rule"));
                        t.biomeLocations.keySet().forEach(biome -> {
                            if (!map.containsKey(biome)) map.put(biome, t.wrappedRule(biome));
                        });
                    });
                });
            }
        });
        map.forEach((biome, value) -> {
            String biomename = biome.substring(biome.lastIndexOf(":") + 1) + ".json";
            String modname = biome.substring(0, biome.lastIndexOf(":"));
            String path = "config/infinity/modular/" + modname + "/surface_rule";
            CommonIO.writeSurfaceRule(value, path, biomename);
        });
        DataCollection.loggerOutput(map.size(), "surface rules");
    }

    class Tree{
        ArrayList<TreeLeaf> registry;
        HashMap<String, ArrayList<Integer>> biomeLocations;

        Tree(NbtCompound surfaceRule) {
            registry = new ArrayList<>();
            biomeLocations = new HashMap<>();
            TreeLeaf root = new TreeLeaf(new NbtCompound(), -1, null, false);
            add(surfaceRule, root);
        }

        static NbtCompound conditionCase(NbtCompound if_true, NbtCompound then_run) {
            NbtCompound c = new NbtCompound();
            c.put("if_true", if_true);
            c.put("then_run", then_run);
            c.putString("type", "minecraft:condition");
            return c;
        }

        void addBiomeLoc(String s, Integer i) {
            if (!biomeLocations.containsKey(s)) biomeLocations.put(s, new ArrayList<>());
            biomeLocations.get(s).add(i);
        }

        TreeLeaf addOfRule(NbtCompound rule, TreeLeaf where, boolean terminal) {
            TreeLeaf l = new TreeLeaf(rule, registry.size(), where, terminal);
            registry.add(l);
            return l;
        }

        void add(NbtCompound rule, TreeLeaf where) {
            switch(NbtUtils.getString(rule,"type", "")) {
                case "condition", "minecraft:condition" -> {
                    NbtCompound next = NbtUtils.getCompound(rule,"then_run");
                    NbtCompound c = NbtUtils.getCompound(rule, "if_true");
                    if (NbtUtils.getString(c, "type").contains("above_preliminary_surface")) {
                        add(next, where);
                    }
                    else if (!NbtUtils.getString(c, "type").contains("biome")) {
                        TreeLeaf l = addOfRule(c, where, false);
                        add(next, l);
                    }
                    else {
                        TreeLeaf l = addOfRule(next, where, true);
                        if(Objects.requireNonNull(c.get("biome_is")).getNbtType().equals(NbtList.TYPE)) {
                            NbtUtils.getList(c, "biome_is", NbtElement.STRING_TYPE).forEach(e -> addBiomeLoc(e.asString(), l.i));
                        }
                    }
                }
                case "sequence", "minecraft:sequence" -> {
                    NbtList sq = NbtUtils.getList(rule, "sequence", NbtElement.COMPOUND_TYPE);
                    sq.forEach(e -> add((NbtCompound)e, where));
                }
                default -> {
                    if (!checkUnneededParts(rule)) {
                        TreeLeaf l = addOfRule(rule, where, true);
                        addBiomeLoc("minecraft:default", l.i);
                    }
                }
            }
        }

        static boolean checkUnneededParts(NbtCompound rule) {
            return NbtUtils.getString(rule, "type", "").contains("block")
                    && (rule.toString().contains("minecraft:bedrock")
                    || rule.toString().contains("minecraft:deepslate"));
        }

        public NbtCompound wrappedRule(String biome) {
            NbtCompound c = new NbtCompound();
            NbtList l = new NbtList();
            l.add(NbtString.of(biome));
            c.put("biomes", l);
            c.put("rule", extractRule(biome));
            return c;
        }

        public NbtCompound extractRule(String biome) {
            if (!biomeLocations.containsKey(biome)) return null;
            else {
                NbtCompound comp = RandomNoisePreset.startingRule("sequence");
                NbtList l = new NbtList();
                try {
                    biomeLocations.get(biome).forEach(i -> l.add(extractRule(i)));
                    if (biomeLocations.containsKey("minecraft:default"))
                        biomeLocations.get("minecraft:default").forEach(i -> l.add(extractRule(i)));
                    else {
                        InfinityMod.LOGGER.warn("Default locations unexpectedly missing when processing surface rules for biome {}", biome);
                    }
                    comp.put("sequence", l);
                } catch (Exception e) {
                    throw new RuntimeException("Encountered an unexpected exception when processing surface rules for biome " + biome + "\n" + e.getMessage());
                }
                biomeLocations.get(biome).forEach(i -> l.add(extractRule(i)));
                if (biomeLocations.containsKey("minecraft:default"))
                    biomeLocations.get("minecraft:default").forEach(i -> l.add(extractRule(i)));
                comp.put("sequence", l);
                return comp;
            }
        }

        TreeLeaf getParent(TreeLeaf l) {
            if (l.i_parent == -1) return l;
            return registry.get(l.i_parent);
        }

        NbtCompound extractRule(int i) {
            TreeLeaf l = registry.get(i);
            assert l.is_terminal;
            if (l.i_parent == -1) return l.data;
            return extractRule(getParent(l), l.data);
        }

        NbtCompound extractRule(TreeLeaf l, NbtCompound data) {
            NbtCompound newdata = conditionCase(l.data, data);
            if (l.i_parent == -1) return newdata;
            return extractRule(getParent(l), newdata);
        }

        static class TreeLeaf{
            NbtCompound data;
            int i;
            int i_parent;
            boolean is_terminal;

            TreeLeaf(NbtCompound c, int num, @Nullable TreeLeaf where, boolean e) {
                data = c;
                i = num;
                if (where == null) {
                    i_parent = -1;
                }
                else {
                    i_parent = where.i;
                }
                is_terminal = e;
            }
        }
    }
}
