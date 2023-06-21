package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomBlobs extends RandomisedFeature {
    NbtCompound block;

    public RandomBlobs(RandomFeaturesList parent) {
        super(parent, "blob");
        block = parent.PROVIDER.randomBlock(random, "full_blocks_worldgen");
        daddy.additional_blocks.add(block);
        id = "netherrack_replace_blobs";
        type = "uniform";
        save(1 + random.nextInt(32));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomIntProvider(config, "radius", 0, 12);
        config.put("state", block);
        config.put("target", daddy.default_block);
        return feature(config);
    }
}
