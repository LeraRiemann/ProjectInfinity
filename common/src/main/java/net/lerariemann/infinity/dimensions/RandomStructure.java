package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.lerariemann.infinity.util.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

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
    public NbtCompound rawdata;
    public NbtCompound data;

    RandomStructure(int i, RandomBiome b) {
        id = i;
        parent = b;
        random = new Random(i);
        rawdata = (NbtCompound)(b.PROVIDER.extraRegistry.get("structures").getRandomElement(random));
        String name_raw = rawdata.getString("name");
        if (name_raw.lastIndexOf(":") < 0) name = name_raw + "_" + i;
        else name = name_raw.substring(0, name_raw.lastIndexOf(":")) + "_" + name_raw.substring(name_raw.lastIndexOf(":") + 1) + "_" + i;
        type = rawdata.getString("type");
        if (type.length() == 0) type = name_raw;
    }

    void save() {
        fullname = InfinityMod.MOD_ID + ":" + name;
        data = rawdata.getCompound("settings");
        data.putString("biomes", parent.fullname);
        data.put("spawn_overrides", spawnOverrides(rawdata));
        RandomDimension daddy = parent.parent;
        if (rawdata.contains("village")) data.putString("start_pool", parent.PROVIDER.randomName(random, "village_start_pools"));
        if (rawdata.contains("jigsaw")) {
            data.putString("type", "jigsaw");
            if (!data.contains("size")) data.putInt("size", random.nextInt(4,8));
            if (!data.contains("start_height")) data.put("start_height", startHeight(rawdata));
            if (!data.contains("max_distance_from_center")) data.putInt("max_distance_from_center", random.nextInt(1,117));
        }
        else data.putString("type", type);
        switch (type) {
            case "nether_fossil" -> data.put("height", RandomProvider.heightProvider(random, daddy.min_y, daddy.min_y + daddy.height, false, true));
            case "ocean_ruin" -> {
                data.putString("biome_temp", roll("ruins_warm") ? "warm" : "cold");
                data.putFloat("large_probability", random.nextFloat());
                data.putFloat("cluster_probability", random.nextFloat());
            }
            case "ruined_portal" -> {
                NbtList setups = new NbtList();
                int j = random.nextInt(1, 7);
                for (int k = 0; k < j; k++) setups.add(newPortalSetup());
                data.put("setups", setups);
            }
            case "shipwreck" -> data.putBoolean("is_beached", roll("shipwrecks_beach"));
            case "mineshaft" -> data.putString("mineshaft_type", roll("mineshafts_mesa") ? "mesa" : "normal");
        }
        CommonIO.write(data, parent.parent.getStoragePath() + "/worldgen/structure", name + ".json");
        (new RandomStructureSet(this)).save();
    }

    NbtCompound newPortalSetup() {
        NbtCompound setup = new NbtCompound();
        setup.putInt("weight", random.nextInt(1, 21));
        setup.putString("placement", parent.PROVIDER.randomName(random, "ruined_portal_placements"));
        setup.putFloat("air_pocket_probability", random.nextFloat());
        setup.putFloat("mossiness", random.nextFloat());
        setup.putBoolean("overgrown", roll("portal_overgrown"));
        setup.putBoolean("vines", roll("portal_vines"));
        setup.putBoolean("can_be_cold", roll("portal_cold"));
        setup.putBoolean("replace_with_blackstone", roll("portal_blackstone"));
        return setup;
    }

    boolean roll(String key) {
        return parent.PROVIDER.roll(random, key);
    }

    NbtCompound startHeight(NbtCompound rawdata) {
        NbtCompound res = new NbtCompound();
        int i;
        if (rawdata.contains("snap_to_sea_level")) i = parent.parent.sea_level + 1;
        else i = random.nextInt(parent.parent.height);
        res.putInt("absolute", i);
        return res;
    }
    NbtCompound spawnOverrides(NbtCompound rawdata) {
        boolean bl = rawdata.contains("spawn_overrides");
        if (roll("spawn_override")) {
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
        return bl ? rawdata.getCompound("spawn_overrides") : new NbtCompound();
    }
}
