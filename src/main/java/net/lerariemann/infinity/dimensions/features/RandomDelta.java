package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;

public class RandomDelta extends RandomisedFeature {
    public RandomDelta(RandomFeaturesList parent) {
        super(parent, "delta");
        id = "delta_feature";
        type = "everylayer";
        save(1 + random.nextInt(50));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        String block = genBlockOrFluid();
        addBlockCarefully(config, "contents", block);
        addRandomBlock(config, "rim", "full_blocks");
        config.put("size", RandomProvider.intProvider(random, 17, true));
        config.put("rim_size", RandomProvider.intProvider(random, 17, true));
        return feature(config);
    }
}
