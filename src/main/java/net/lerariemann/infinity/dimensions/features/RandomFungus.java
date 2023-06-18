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
        config.put("hat_state", PROVIDER.randomBlock(random, "full_blocks"));
        config.put("decor_state", PROVIDER.randomBlock(random, "blocks_features"));
        config.put("stem_state", PROVIDER.randomBlock(random, "full_blocks"));
        addBlockCarefully(config, "valid_base_block", mainsurfaceblock);
        return feature(config);
    }
}
