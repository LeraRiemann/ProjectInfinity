package net.lerariemann.infinity.dimensions.features.surface_structures;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomWell extends RandomisedFeature {
    public RandomWell(RandomFeaturesList lst) {
        super(lst, "well");
        savePlacement("minecraft:desert_well");
    }

    public NbtCompound feature() {
        return null;
    }
    public NbtList placement() {
        Placement res = new Placement();
        res.addRarityFilter(random.nextInt(1, 1000));
        res.addInSquare();
        res.addHeightmap("MOTION_BLOCKING");
        res.addBiome();
        return res.data;
    }
}
