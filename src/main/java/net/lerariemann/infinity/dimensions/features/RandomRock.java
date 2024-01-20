package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomRock extends RandomisedFeature {
    public RandomRock(RandomFeaturesList parent) {
        super(parent, "rock");
        id = "forest_rock";
        save_with_placement();
    }

    void placement() {
        addCount(1 + random.nextInt(8));
        addInSquare();
        addHeightmap("MOTION_BLOCKING");
        addBiome();
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "state", "full_blocks");
        return feature(config);
    }
}
