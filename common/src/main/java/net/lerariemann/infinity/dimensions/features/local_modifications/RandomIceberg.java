package net.lerariemann.infinity.dimensions.features.local_modifications;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomIceberg extends RandomisedFeature {
    public RandomIceberg(RandomFeaturesList parent) {
        super(parent, "iceberg");
        savePlacement();
    }

    public NbtList placement() {
        Placement res = new Placement();
        res.addRarityFilter(1 + random.nextInt(16));
        res.addInSquare();
        res.addBiome();
        return res.data;
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "state", ConfigType.FULL_BLOCKS_WG);
        return feature(config);
    }
}
