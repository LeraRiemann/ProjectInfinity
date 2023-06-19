package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomColumns extends RandomisedFeature {
    public RandomColumns(RandomFeaturesList parent) {
        super(parent, "columns");
        id = "random_columns";
        type = "everylayer";
        save(1 + random.nextInt(4));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomIntProvider(config, "reach", 0, 3);
        addRandomIntProvider(config, "height", 1, 15);
        addRandomBlockProvider(config, "block_provider", "full_blocks_worldgen");
        return feature(config);
    }
}
