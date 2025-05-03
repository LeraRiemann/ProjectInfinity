package net.lerariemann.infinity.dimensions.features.underground_decoration;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomBlobs extends RandomisedFeature {
    NbtCompound block;

    public RandomBlobs(RandomFeaturesList parent) {
        super(parent, "blob");
        block = parent.PROVIDER.randomElement(random, ConfigType.FULL_BLOCKS_WG);
        daddy.additional_blocks.add(block);
        id = "netherrack_replace_blobs";
        savePlacement();
    }

    public NbtList placement() {
        return Placement.uniform(1 + random.nextInt(32));
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomIntProvider(config, "radius", 0, 12);
        config.put("state", block);
        config.put("target", daddy.default_block);
        return feature(config);
    }
}
