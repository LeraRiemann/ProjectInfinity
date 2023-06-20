package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomFungus extends RandomisedFeature {
    String mainsurfaceblock;

    public RandomFungus(RandomFeaturesList parent) {
        super(parent, "fungus");
        id = "huge_fungus";
        type = "everylayer";
        mainsurfaceblock = parent.surface_block;
        save(1 + random.nextInt(4));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "hat_state", "full_blocks");
        addRandomBlock(config, "decor_state", "blocks_features");
        addRandomBlock(config, "stem_state", "full_blocks");
        addBlockCarefully(config, "valid_base_block", mainsurfaceblock);
        return feature(config);
    }
}
