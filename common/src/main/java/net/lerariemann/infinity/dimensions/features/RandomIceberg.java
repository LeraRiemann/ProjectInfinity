package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;

public class RandomIceberg extends RandomisedFeature {
    public RandomIceberg(RandomFeaturesList parent) {
        super(parent, "iceberg");
        save_with_placement();
    }

    void placement() {
        addRarityFilter(1 + random.nextInt(16));
        addInSquare();
        addBiome();
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "state", ConfigType.FULL_BLOCKS_WG);
        return feature(config);
    }
}
