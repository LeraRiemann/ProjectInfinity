package net.lerariemann.infinity.dimensions.features.surface_structures;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomColumns extends RandomisedFeature {
    public RandomColumns(RandomFeaturesList parent) {
        super(parent, "columns");
        id = "infinity:random_columns";
        savePlacement();
    }

    public NbtList placement() {
        return Placement.everylayerBiome(1 + random.nextInt(4));
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomIntProvider(config, "reach", 0, 3);
        addRandomIntProvider(config, "height", 1, 15);
        addRandomBlockProvider(config, "block_provider", ConfigType.FULL_BLOCKS_WG);
        return feature(config);
    }
}
