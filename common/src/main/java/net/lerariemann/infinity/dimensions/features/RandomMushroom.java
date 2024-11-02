package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomMushroom extends RandomisedFeature {
    public RandomMushroom(RandomFeaturesList parent) {
        super(parent, "mushroom");
        id = random.nextBoolean() ? "infinity:random_flat_mushroom" : "infinity:random_round_mushroom";
        save_with_placement();
    }

    void placement() {
        addCountEveryLayer(1);
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlockProvider(config, "cap_provider", "full_blocks_worldgen");
        addRandomBlockProvider(config, "stem_provider", "full_blocks_worldgen");
        config.put("valid_base_block", parent.surface_block);
        config.putInt("foliage_radius", random.nextBoolean() ? 2 : 1 + (int)Math.floor(random.nextExponential()*2));
        config.putInt("height", random.nextBoolean() ? 5 : random.nextInt(3, 7));
        return feature(config);
    }
}
