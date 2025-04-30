package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.NbtCompound;

public class RandomDelta extends RandomisedFeature {
    public RandomDelta(RandomFeaturesList parent) {
        super(parent, "delta");
        id = "delta_feature";
        save_with_placement();
    }

    void placement() {
        placement_everylayer_biome(1 + random.nextInt(50));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtCompound block = genBlockOrFluid();
        config.put("contents", block);
        addRandomBlock(config, "rim", ConfigType.FULL_BLOCKS);
        config.put("size", NbtUtils.randomIntProvider(random, 17, true));
        config.put("rim_size", NbtUtils.randomIntProvider(random, 17, true));
        return feature(config);
    }
}
