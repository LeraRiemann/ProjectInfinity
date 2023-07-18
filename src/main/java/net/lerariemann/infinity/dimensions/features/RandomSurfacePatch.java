package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.util.CommonIO;
import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomSurfacePatch extends RandomisedFeature {
    public RandomSurfacePatch(RandomFeaturesList parent) {
        super(parent, "patch");
        id = "random_patch";
        type = "surfacepatch";
        save(1 + random.nextInt(64));
    }

    NbtCompound feature() {
        int xz_spread = 2 + random.nextInt(14);
        int y_spread = 1 + random.nextInt(7);
        int tries_max = (xz_spread+1)*(xz_spread+1);
        NbtCompound config = CommonIO.readCarefully(PROVIDER.configPath + "util/preplacements/surfacepatch.json",
                CommonIO.CompoundToString(PROVIDER.randomBlock(random, "blocks_features"), 0),
                parent.surface_block.getString("Name"), Math.min(256, random.nextInt(tries_max)), xz_spread, y_spread);
        return feature(config);
    }
}
