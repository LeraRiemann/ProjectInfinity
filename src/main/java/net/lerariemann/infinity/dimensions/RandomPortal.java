package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class RandomPortal {
    public RandomBiome parent;
    public int id;
    public String type;
    public String name;
    public String fullname;
    public final Random random;
    public NbtCompound data;
    public NbtCompound dataset;
    RandomPortal(int i, RandomBiome b) {
        id = i;
        parent = b;
        random = new Random(i);
        type = "portal";
        name = type + "_" + i;
        fullname = InfinityMod.MOD_ID + ":" + name;
        NbtCompound rawdata = (NbtCompound)(b.PROVIDER.extraRegistry.get("palettes").getRandomElement(random));
        data = CommonIO.readCarefully(b.PROVIDER.configPath + "util/portal/settings.json", b.fullname,
                InfinityMod.MOD_ID + ":" + rawdata.getString("name"));
        int a = random.nextInt(100);
        int c = random.nextInt(100);
        int d = random.nextInt();
        dataset = CommonIO.readCarefully(b.PROVIDER.configPath + "util/portal/set.json", fullname,
                max(d, -d), max(a, c), min(a, c), random.nextDouble());
    }

    void save() {
        CommonIO.write(data, parent.parent.storagePath + "/worldgen/structure", name + ".json");
        CommonIO.write(dataset, parent.parent.storagePath + "/worldgen/structure_set", name + ".json");
    }
}
