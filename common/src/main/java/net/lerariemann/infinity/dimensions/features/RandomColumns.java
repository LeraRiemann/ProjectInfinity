package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;

public class RandomColumns extends RandomisedFeature {
    public RandomColumns(RandomFeaturesList parent) {
        super(parent, "columns");
        id = "infinity:random_columns";
        save_with_placement();
    }

    void placement() {
        placement_everylayer_biome(1 + random.nextInt(4));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomIntProvider(config, "reach", 0, 3);
        addRandomIntProvider(config, "height", 1, 15);
        addRandomBlockProvider(config, "block_provider", ConfigType.FULL_BLOCKS_WG);
        return feature(config);
    }
}
