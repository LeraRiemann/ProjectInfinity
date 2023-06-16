package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

import java.util.List;

public class RandomFungus extends RandomisedFeature {
    List<String> validbaseblocks;
    String mainsurfaceblock;

    public RandomFungus(RandomFeaturesList parent) {
        super(parent, "fungus");
        id = "huge_fungus";
        type = "everylayer";
        validbaseblocks = parent.blocks;
        mainsurfaceblock = parent.surface_block;
        save(1 + random.nextInt(10));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "hat_state", "full_blocks");
        addRandomBlock(config, "decor_state", "blocks_features");
        addRandomBlock(config, "stem_state", "full_blocks");
        String base_block = mainsurfaceblock;
        addBlockCarefully(config, "valid_base_block", base_block);
        return feature(config);
    }
}
