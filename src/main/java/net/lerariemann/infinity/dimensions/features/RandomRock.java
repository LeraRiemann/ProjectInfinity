package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomRock extends RandomisedFeature {
    public RandomRock(RandomFeaturesList parent) {
        super(parent, "rock");
        id = type = "forest_rock";
        save(1 + random.nextInt(8));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "state", "full_blocks");
        return feature(config);
    }
}
