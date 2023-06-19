package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.util.CommonIO;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomFloatingPatch extends RandomisedFeature {
    public RandomFloatingPatch(RandomFeaturesList parent) {
        super(parent, "patch_floating");
        id = "random_patch";
        type = "floatingpatch";
        RandomDimension dim = parent.parent.parent;
        int a = dim.min_y + random.nextInt(dim.height);
        int b = dim.min_y + random.nextInt(dim.height);
        int min_inclusive = Math.min(a, b);
        int max_inclusive = Math.max(a, b);
        save(min_inclusive, max_inclusive);
    }

    NbtCompound feature() {
        int xz_spread = 2 + random.nextInt(14);
        int y_spread = 1 + random.nextInt(7);
        int tries_max = (xz_spread+1)*(xz_spread+1);
        NbtCompound config = CommonIO.readCarefully(PROVIDER.configPath + "features/preplacements/floatingpatch.json",
                CommonIO.CompoundToString(PROVIDER.randomBlock(random, "blocks_features"), 0),
                Math.min(256, random.nextInt(tries_max)), xz_spread, y_spread);
        return feature(config);
    }
}
