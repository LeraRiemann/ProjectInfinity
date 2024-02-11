package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.util.CommonIO;
import net.lerariemann.infinity.var.ModCommands;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Pair;

import java.util.HashMap;
import java.util.Map;


public class Easterizer {
    Map<Long, Pair<Integer, Integer>> map;

    public Easterizer(NbtList maps, RandomProvider prov) {
        map = new HashMap<>();
        for (NbtElement c : maps) {
            Pair<Integer, Integer> easter_pair = new Pair<>(((NbtCompound)c).getInt("easter-id"), ((NbtCompound)c).getInt("easter-type"));
            Long l = ModCommands.getDimensionSeedFromText(((NbtCompound)c).getString("text"), prov);
            map.put(l, easter_pair);
        }
    }

    public static boolean easterize(RandomDimension d) {
        Map<Long, Pair<Integer, Integer>> map = d.PROVIDER.easterizer.map;
        if (!map.containsKey(d.id)) return false;
        d.type = new RandomDimensionType(d, CommonIO.read(d.PROVIDER.configPath + "util/easter/" + map.get(d.id).getRight() + "_type.json"));
        d.data.putString("type", d.type.fullname);
        d.data.put("generator", CommonIO.read(d.PROVIDER.configPath + "util/easter/" + map.get(d.id).getLeft() + ".json"));
        return true;
    }
}
