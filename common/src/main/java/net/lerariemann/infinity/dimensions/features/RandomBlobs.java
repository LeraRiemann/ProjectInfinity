package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;

public class RandomBlobs extends RandomisedFeature {
    NbtCompound block;

    public RandomBlobs(RandomFeaturesList parent) {
        super(parent, "blob");
        block = parent.PROVIDER.randomElement(random, ConfigType.FULL_BLOCKS_WG);
        daddy.additional_blocks.add(block);
        id = "netherrack_replace_blobs";
        save_with_placement();
    }

    void placement() {
        placement_uniform(1 + random.nextInt(32));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomIntProvider(config, "radius", 0, 12);
        config.put("state", block);
        config.put("target", daddy.default_block);
        return feature(config);
    }
}
