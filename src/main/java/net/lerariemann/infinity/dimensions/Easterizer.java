package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.lerariemann.infinity.var.ModCommands;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.walk;


public class Easterizer {
    public Map<Long, Pair<NbtCompound, Pair<String, String>>> map;
    public Map<String, NbtCompound> shadermap;

    public Easterizer(RandomProvider prov) {
        map = new HashMap<>();
        shadermap = new HashMap<>();
        try {
            walk(Paths.get(prov.configPath + "util/easter")).forEach(p -> {
                String fullname = p.toString();
                if (p.toFile().isFile() && !fullname.endsWith("_type.json")) {
                    String name = fullname.substring(fullname.lastIndexOf("/") + 1, fullname.length() - 5);
                    String type = "default";
                    NbtCompound compound = CommonIO.read(p.toFile());
                    if (compound.contains("easter-name")) {
                        name = compound.getString("easter-name");
                        compound.remove("easter-name");
                    }
                    if (compound.contains("easter-type")) {
                        type = compound.getString("easter-type");
                        compound.remove("easter-type");
                    }
                    if (compound.contains("easter-shader")) {
                        shadermap.put("infinity:" + name, compound.getCompound("easter-shader"));
                        compound.remove("easter-shader");
                    }
                    Long l = ModCommands.getDimensionSeed(name, prov);
                    Pair<NbtCompound, Pair<String, String>> easter_pair = new Pair<>(compound, new Pair<>(name, type));
                    map.put(l, easter_pair);
                }});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean easterize(RandomDimension d) {
        Map<Long, Pair<NbtCompound, Pair<String, String>>> map = d.PROVIDER.easterizer.map;
        if (!map.containsKey(d.id)) return false;
        d.data.putString("type", InfinityMod.MOD_ID + ":" + map.get(d.id).getRight().getRight() + "_type");
        d.data.put("generator", map.get(d.id).getLeft());
        return true;
    }

    public boolean isEaster(long d) {
        return map.containsKey(d);
    }

    public String keyOf(long d) {
        if (!isEaster(d)) return "generated_" + d;
        return map.get(d).getRight().getLeft();
    }
}
