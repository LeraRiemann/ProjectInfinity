package net.lerariemann.infinity.util;

import net.lerariemann.infinity.dimensions.RandomNoisePreset;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface SurfaceRuleScanner {
    static Set<String> scan(MinecraftServer server) {
        Map<String, NbtCompound> map = new HashMap<>();
        Registry<ChunkGeneratorSettings> registry = server.getRegistryManager().get(RegistryKeys.CHUNK_GENERATOR_SETTINGS);
        registry.getKeys().forEach(key -> {
            if (!key.getValue().getNamespace().contains("infinity")) {
                Optional<ChunkGeneratorSettings> o = registry.getOrEmpty(key);
                o.ifPresent(settings -> {
                    Optional<NbtElement> c = ChunkGeneratorSettings.CODEC.encodeStart(NbtOps.INSTANCE, settings).result();
                    c.ifPresent(e -> {
                        Tree t = new Tree(((NbtCompound) e).getCompound("surface_rule"));
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
        return map.keySet();
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
            switch(rule.getString("type")) {
                case "condition", "minecraft:condition" -> {
                    NbtCompound next = rule.getCompound("then_run");
                    NbtCompound c = rule.getCompound("if_true");
                    if (c.getString("type").contains("above_preliminary_surface")) {
                        add(next, where);
                    }
                    else if (!c.getString("type").contains("biome")) {
                        TreeLeaf l = addOfRule(c, where, false);
                        add(next, l);
                    }
                    else {
                        TreeLeaf l = addOfRule(next, where, true);
                        if(Objects.requireNonNull(c.get("biome_is")).getNbtType().equals(NbtList.TYPE)) {
                            c.getList("biome_is", NbtElement.STRING_TYPE).forEach(e -> addBiomeLoc(e.asString(), l.i));
                        }
                    }
                }
                case "sequence", "minecraft:sequence" -> {
                    NbtList sq = rule.getList("sequence", NbtElement.COMPOUND_TYPE);
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
            return rule.getString("type").contains("block") && rule.toString().contains("minecraft:bedrock");
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
            if (biomeLocations.get(biome).size() == 1) return extractRule(biomeLocations.get(biome).getFirst());
            else {
                NbtCompound comp = RandomNoisePreset.startingRule("sequence");
                NbtList l = new NbtList();
                biomeLocations.get(biome).forEach(i -> l.add(extractRule(i)));
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
