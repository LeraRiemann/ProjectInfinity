package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomIceberg extends RandomisedFeature {
    public RandomIceberg(RandomFeaturesList parent) {
        super(parent, "iceberg");
        type = "iceberg";
        save(1 + random.nextInt(16));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "state");
        return feature(config);
    }
}
