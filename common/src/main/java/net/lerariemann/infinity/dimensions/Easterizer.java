package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.walk;


public class Easterizer {
    public Map<String, Pair<NbtCompound, String>> map;
    public Map<String, NbtCompound> optionmap;

    public Easterizer(RandomProvider prov) {
        map = new HashMap<>();
        optionmap = new HashMap<>();
        try {
            walk(Paths.get(prov.configPath).resolve("util").resolve("easter")).forEach(p -> {
                String fullname = p.toString();
                if (p.toFile().isFile() && !fullname.endsWith("_type.json")) {
                    String name = p.getFileName().toString();
                    name = name.substring(0, name.length() - 5);
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
                    if (compound.contains("easter-options")) {
                        optionmap.put(name, compound.getCompound("easter-options"));
                        compound.remove("easter-options");
                    }
                    map.put(name, new Pair<>(compound, type));
                }});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean easterize(RandomDimension d) {
        Map<String, Pair<NbtCompound, String>> map = d.PROVIDER.easterizer.map;
        String name = d.getName();
        if (!map.containsKey(d.getName())) return false;
        d.data.putString("type", InfinityMod.MOD_ID + ":" + map.get(name).getRight() + "_type");
        d.data.put("generator", map.get(name).getLeft());
        return true;
    }

    public boolean isEaster(String name) {
        return map.containsKey(name);
    }
}
