package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomFungus extends RandomisedFeature {
    NbtCompound mainsurfaceblock;

    public RandomFungus(RandomFeaturesList parent) {
        super(parent, "fungus");
        id = "huge_fungus";
        type = "everylayer";
        mainsurfaceblock = parent.surface_block;
        save(1);
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "hat_state", "full_blocks_worldgen");
        addRandomBlock(config, "decor_state", "blocks_features");
        addRandomBlock(config, "stem_state", "full_blocks_worldgen");
        NbtCompound replaceableBlocks = new NbtCompound();
        replaceableBlocks.putString("type", "minecraft:matching_block_tag");
        replaceableBlocks.putString("tag", PROVIDER.randomName(random, "tags").replace("#", ""));
        config.put("replaceable_blocks", replaceableBlocks);
        config.put("valid_base_block", mainsurfaceblock);
        return feature(config);
    }
}
