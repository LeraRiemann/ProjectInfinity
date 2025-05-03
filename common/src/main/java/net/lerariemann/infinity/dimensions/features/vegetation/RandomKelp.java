package net.lerariemann.infinity.dimensions.features.vegetation;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomKelp extends RandomisedFeature {
    public RandomKelp(RandomFeaturesList lst) {
        super(lst.parent.id, lst, "kelp");
        savePlacement("minecraft:kelp");
    }
    public NbtCompound feature() {
        return null;
    }
    public NbtCompound noiseCount() {
        NbtCompound res = Placement.ofType("noise_based_count");
        res.putDouble("noise_factor", 80.0);
        res.putDouble("noise_offset", 0.0);
        res.putInt("noise_to_count_ratio", 20 + random.nextInt(120));
        return res;
    }

    public NbtList placement() {
        Placement res = new Placement();
        res.data.add(noiseCount());
        res.addInSquare();
        res.addHeightmap("OCEAN_FLOOR_WG");
        res.addBiome();
        return res.data;
    }
}
