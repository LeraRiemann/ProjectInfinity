package net.lerariemann.infinity.dimensions.features.local_modifications;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomRock extends RandomisedFeature {
    public RandomRock(RandomFeaturesList parent) {
        super(parent, "rock");
        id = "forest_rock";
        savePlacement();
    }

    public NbtList placement() {
        Placement res = new Placement();
        res.addCount(1 + random.nextInt(8));
        res.addInSquare();
        res.addHeightmap("MOTION_BLOCKING");
        res.addBiome();
        return res.data;
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "state", ConfigType.FULL_BLOCKS);
        return feature(config);
    }
}
