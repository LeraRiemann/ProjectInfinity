package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandomStructure {
    public RandomBiome parent;
    public int id;
    public String type;
    public String name;
    public String fullname;
    public Random random;
    public NbtCompound data;

    RandomStructure(int i, RandomBiome b) {
        id = i;
        parent = b;
        random = new Random(i);
        data = (NbtCompound)(b.PROVIDER.compoundRegistry.get("structures").getRandomElement(random));
        type = data.getString("id");
        name = new Identifier(type).getPath() + "_" + i;
    }

    void save() {
        fullname = InfinityMod.MOD_ID + ":" + name;
        data.putString("type", "infinity:setupper");
        data.putString("biomes", parent.fullname);
        if (roll("spawn_override")) {
            data.remove("spawn_overrides");
            data.put("spawn_overrides", spawnOverrides());
        }
        CommonIO.write(data, parent.parent.getStoragePath() + "/worldgen/structure", name + ".json");
        (new RandomStructureSet(this)).save();
    }

    boolean roll(String key) {
        return parent.PROVIDER.roll(random, key);
    }

    NbtCompound spawnOverrides() {
        NbtCompound res = new NbtCompound();
        Map<String, NbtList> moblist = new HashMap<>();
        int nummobs = random.nextInt(1, 7);
        for (int i = 0; i < nummobs; i++) {
            String mobcategory = parent.PROVIDER.randomName(random, "mob_categories");
            NbtCompound mob = parent.addMob(mobcategory, false);
            if (!moblist.containsKey(mobcategory)) moblist.put(mobcategory, new NbtList());
            moblist.get(mobcategory).add(mob);
        }
        for (String i : moblist.keySet()) {
            NbtCompound category = new NbtCompound();
            category.putString("bounding_box", roll("full_box") ? "full" : "piece");
            category.put("spawns", new NbtList());
            res.put(i, category);
        }
        return res;
    }
}
