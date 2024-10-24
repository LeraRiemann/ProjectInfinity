package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomDungeon extends RandomisedFeature {
    public RandomDungeon(RandomFeaturesList parent) {
        super(parent, "dungeon");
        id = "infinity:random_dungeon";
        save_with_placement();
    }

    void placement() {
        placement_uniform(10 + random.nextInt(200));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "main_state", "full_blocks_worldgen");
        addRandomBlock(config, "decor_state", "full_blocks");
        config.putString("mob", PROVIDER.randomName(random, "mobs"));
        config.putInt("size", 1 + random.nextInt(6));
        return feature(config);
    }
}
