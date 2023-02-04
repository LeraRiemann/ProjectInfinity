package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomEndIsland extends RandomisedFeature {
    public RandomEndIsland(RandomFeaturesList parent) {
        super(parent, "island");
        id = "random_end_island";
        type = "endisland";
        RandomDimension dim = parent.parent.parent;
        int a = (int)random.nextGaussian(dim.sea_level, 16);
        int b = random.nextInt(dim.sea_level, dim.height + dim.min_y);
        int min_inclusive = Math.min(b, Math.max(a, dim.min_y));
        int max_inclusive = Math.max(a, b);
        save(1 + random.nextInt(16), max_inclusive, min_inclusive);
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "state");
        return feature(config);
    }
}
