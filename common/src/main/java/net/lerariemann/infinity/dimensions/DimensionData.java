package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public class DimensionData {
    NbtCompound data;
    String path;
    DimensionData(RandomDimension parent) {
        data = new NbtCompound();
        path = parent.getStoragePath();
        data.put("stats", stats(parent));
        data.put("blocks", blocks(parent));
        data.put("biomes", biomes(parent));
        data.put("structures", structures(parent));
    }

    void save() {
        CommonIO.write(data, path, "data.json");
    }

    NbtCompound stats(RandomDimension parent) {
        NbtCompound res = new NbtCompound();
        res.putString("type", parent.type_alike);
        res.putInt("height", parent.height);
        res.putInt("min_y", parent.min_y);
        res.putInt("sea_level", parent.sea_level);
        return res;
    }

    NbtCompound blocks(RandomDimension parent) {
        NbtCompound res = new NbtCompound();
        res.put("default", parent.default_block);
        res.put("deepslate", parent.deepslate);
        res.put("fluid", parent.default_fluid);
        return res;
    }

    NbtCompound biomes(RandomDimension parent) {
        NbtCompound res = new NbtCompound();
        NbtList vanilla = new NbtList();
        parent.vanilla_biomes.forEach(s -> vanilla.add(NbtString.of(s)));
        res.put("vanilla", vanilla);
        NbtList random = new NbtList();
        parent.random_biomes.forEach(biome -> {
            NbtCompound b = new NbtCompound();
            String name = biome.fullname;
            b.putString("name", name);
            b.put("grass", parent.top_blocks.get(name));
            b.put("dirt", parent.underwater.get(name));
            b.put("colors", biome.colors);
            NbtList mobs = new NbtList();
            biome.mobs.forEach(mob -> mobs.add(NbtString.of(mob)));
            b.put("mobs", mobs);
            random.add(b);
        });
        res.put("random", random);
        return res;
    }

    NbtCompound structures(RandomDimension parent) {
        NbtCompound res = new NbtCompound();
        parent.structure_ids.forEach((type, list) -> {
            NbtList ids = new NbtList();
            list.forEach(id -> ids.add(NbtString.of(id)));
            res.put(type, ids);
        });
        return res;
    }
}
