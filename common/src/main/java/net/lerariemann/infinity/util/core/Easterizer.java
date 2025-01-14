package net.lerariemann.infinity.util.core;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.nio.file.Files.walk;


public class Easterizer {
    public Map<String, Pair<NbtCompound, String>> map;
    public Map<String, NbtCompound> optionmap;
    public Map<String, Integer> colormap;

    public Easterizer() {
        map = new HashMap<>();
        optionmap = new HashMap<>();
        colormap = new HashMap<>();
        try (Stream<Path> files = walk(InfinityMod.configPath.resolve("easter"))) {
            files.filter(p -> p.toFile().isFile() && !p.toString().endsWith("_type.json")).forEach(p -> {
                String name = p.getFileName().toString().replace(".json", "");
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
                if (compound.contains("easter-color")) {
                    colormap.put(name, compound.getInt("easter-color"));
                    compound.remove("easter-color");
                }
                if (compound.contains("easter-options")) {
                    optionmap.put(name, compound.getCompound("easter-options"));
                    compound.remove("easter-options");
                }
                map.put(name, new Pair<>(compound, type));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean easterize(RandomDimension d) {
        String name = d.getName();
        if (!isEaster(d.getName(), d.PROVIDER)) return false;
        d.data.putString("type", InfinityMod.MOD_ID + ":" + map.get(name).getRight() + "_type");
        d.data.put("generator", map.get(name).getLeft());
        return true;
    }

    public static boolean isDisabled(String name, RandomProvider provider) {
        return provider.disabledDimensions.contains(name);
    }

    public boolean isEaster(String name, RandomProvider provider) {
        return map.containsKey(name) && !isDisabled(name, provider);
    }
}
