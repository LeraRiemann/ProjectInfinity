package net.lerariemann.infinity.util.core;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.nio.file.Files.walk;


public class Easterizer {
    public Map<String, String> aliasMap = new HashMap<>();
    public Map<String, NbtCompound> map = new HashMap<>();
    public Map<String, String> typeMap = new HashMap<>();
    public Map<String, NbtCompound> optionmap = new HashMap<>();

    public Easterizer() {
        try (Stream<Path> files = walk(InfinityMod.configPath.resolve("easter"))) {
            files.filter(p -> p.toFile().isFile() && !p.toString().endsWith("_type.json")).forEach(p -> {
                String name = p.getFileName().toString().replace(".json", "");
                NbtCompound compound = CommonIO.read(p.toFile());
                if (compound.contains("name"))
                    name = compound.getString("name");
                if (compound.contains("type"))
                    typeMap.put(name, compound.getString("type"));
                if (compound.contains("aliases", NbtElement.LIST_TYPE)) {
                    String finalName = name;
                    compound.getList("aliases", NbtElement.STRING_TYPE)
                            .stream()
                            .map(e -> (NbtString)e)
                            .map(NbtString::asString)
                            .forEach(alias -> aliasMap.put(alias, finalName));
                }
                else if (compound.contains("aliases", NbtElement.STRING_TYPE))
                    aliasMap.put(compound.getString("aliases"), name);
                if (compound.contains("options")) {
                    optionmap.put(name, compound.getCompound("options"));
                }

                map.put(name, compound.getCompound("generator"));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean easterize(RandomDimension d) {
        String name = d.getName();
        if (!isEaster(d.getName())) return false;
        String type = typeMap.getOrDefault(name, "default");
        if (!type.contains(":")) type = InfinityMod.MOD_ID + ":" + type;
        d.data.putString("type", type);
        d.data.put("generator", map.get(name));
        return true;
    }

    public static boolean isDisabled(String name) {
        return InfinityMod.provider.disabledDimensions.contains(name);
    }

    public String getAsEaster(String name) {
        if (aliasMap.containsKey(name)) return aliasMap.get(name);
        if (isEaster(name)) return name;
        return null;
    }

    public boolean isEaster(String name) {
        return map.containsKey(name) && !isDisabled(name);
    }
}
