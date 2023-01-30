package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;

public class RandomIceberg extends RandomisedFeature {
    public RandomIceberg(int i, RandomProvider provider, String path) {
        super(i, provider);
        name = "iceberg_" + i;
        id = type = "iceberg";
        save(path,1 + random.nextInt(16));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "state");
        return feature(config);
    }
}
