package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;

public class RandomRock extends RandomisedFeature {
    public RandomRock(int i, RandomProvider provider, String path) {
        super(i, provider);
        name = "rock_" + i;
        id = type = "forest_rock";
        save(path,1 + random.nextInt(8));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "state");
        return feature(config);
    }
}
