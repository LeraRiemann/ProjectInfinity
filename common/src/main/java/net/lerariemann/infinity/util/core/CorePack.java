package net.lerariemann.infinity.util.core;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.nio.file.Path;
import java.util.List;

public record CorePack(RandomProvider provider, Path savingPath) {
    void generate() {
        saveTrees();
        saveEndOfTime();
        if (!savingPath.resolve("pack.mcmeta").toFile().exists()) savePackMcmeta();
    }

    void savePackMcmeta() {
        NbtCompound res = new NbtCompound();
        NbtCompound pack = new NbtCompound();
        pack.putInt("pack_format", 34);
        pack.putString("description", "Common content providers for Infinite Dimensions");
        res.put("pack", pack);
        CommonIO.write(res, savingPath, "pack.mcmeta");
    }

    public static NbtCompound treePlacement(String tree, String block) {
        return CommonIO.readAndFormat(InfinityMod.utilPath.resolve("placements/tree_vanilla.json").toString(), tree, block);
    }

    void saveTrees() {
        WeighedStructure<NbtElement> treesReg = provider.compoundRegistry.get("trees");
        if (treesReg == null) return;
        List<String> trees = treesReg.keys.stream().map(compound -> ((NbtCompound)compound).getString("Name")).toList();
        double size = trees.size();
        NbtCompound c = new NbtCompound();
        NbtList l = new NbtList();
        c.put("default", treePlacement(trees.getFirst(), "minecraft:grass_block"));
        for (int i = 1; i < size; i++) {
            NbtCompound c1 = new NbtCompound();
            c1.put("feature", treePlacement(trees.get(i), "minecraft:grass_block"));
            c1.putDouble("chance", 1 / (size - i + 1));
            l.add(c1);
        }
        c.put("features", l);
        NbtCompound c2 = new NbtCompound();
        c2.putString("type", "minecraft:random_selector");
        c2.put("config", c);
        CommonIO.write(c2, savingPath.resolve("data/" + InfinityMod.MOD_ID + "/worldgen/configured_feature"), "all_trees.json");
    }

    void saveEndOfTime() {
        NbtCompound res = new NbtCompound();
        for (String category : provider.getMobCategories()) {
            NbtList entries = new NbtList();
            for (String mob: provider.registry.get(category).keys) {
                NbtCompound entry = new NbtCompound();
                entry.putString("type", mob);
                entry.putInt("minCount", 1);
                entry.putInt("maxCount", 1);
                entry.putInt("weight", 1);
                entries.add(entry);
            }
            res.put(category, entries);
        }
        CommonIO.write(CommonIO.readAndAddCompound(InfinityMod.utilPath.resolve("end_of_time.json").toString(), res),
                savingPath.resolve("data/" + InfinityMod.MOD_ID + "/worldgen/biome"), "end.json");
    }
}
