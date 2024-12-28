package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.Random;

public class RandomStructureSet {
    public RandomStructure parent;
    public Random random;
    public NbtCompound data;

    RandomStructureSet(RandomStructure str) {
        parent = str;
        random = parent.random;
        data = new NbtCompound();
        data.put("structures", structures());
        data.put("placement", placement());
    }

    void save() {
        CommonIO.write(data, parent.parent.parent.getStoragePath() + "/worldgen/structure_set", parent.name + ".json");
    }

    NbtList structures() {
        NbtList res = new NbtList();
        NbtCompound structure = new NbtCompound();
        structure.putString("structure", parent.fullname);
        structure.putInt("weight", 1);
        res.add(structure);
        return res;
    }

    int f(Random r, int mx, int med) {
        return Math.max(0, Math.min(mx, (int)(Math.floor(r.nextExponential()*med))));
    }

    NbtCompound placement() {
        NbtCompound res = new NbtCompound();
        res.putInt("salt", random.nextInt(Integer.MAX_VALUE));
        res.putFloat("frequency", random.nextFloat());
        String type = parent.parent.PROVIDER.randomName(random, "structure_placement_types");
        res.putString("type", type);
        switch (type) {
            case "minecraft:random_spread" -> {
                if (parent.roll("triangular_spread")) res.putString("spread_type", "triangular");
                int a = 6 + f(random, 4095, 6);
                int b = f(random, 4095, 6);
                if (a == b) a+=1;
                res.putInt("spacing", Math.max(a, b));
                res.putInt("separation", Math.min(a, b));
            }
            case "minecraft:concentric_rings" -> {
                res.putInt("distance", f(random, 1023, 8));
                res.putInt("count", 1 + f(random, 4094, 64));
                res.putInt("spread", 1 + f(random, 1023, 3));
                res.putString("preferred_biomes", parent.parent.fullname);
            }
        }
        return res;
    }
}
