package net.lerariemann.infinity.dimensions;

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
    Map<Long, Pair<NbtCompound, String>> map;

    public Easterizer(RandomProvider prov) {
        map = new HashMap<>();
        try {
            walk(Paths.get(prov.configPath + "util/easter")).forEach(p -> {
                String fullname = p.toString();
                if (p.toFile().isFile() && !fullname.endsWith("_type.json")) {
                    String name = fullname.substring(fullname.lastIndexOf("/") + 1, fullname.length() - 5);
                    String id = name;
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
                    Long l = ModCommands.getDimensionSeed(name, prov);
                    Pair<NbtCompound, String> easter_pair = new Pair<>(compound, type);
                    map.put(l, easter_pair);
                }});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean easterize(RandomDimension d) {
        Map<Long, Pair<NbtCompound, String>> map = d.PROVIDER.easterizer.map;
        if (!map.containsKey(d.id)) return false;
        d.type = new RandomDimensionType(d, CommonIO.read(d.PROVIDER.configPath + "util/easter/" + map.get(d.id).getRight() + "_type.json"));
        d.data.putString("type", d.type.fullname);
        d.data.put("generator", map.get(d.id).getLeft());
        return true;
    }
}
