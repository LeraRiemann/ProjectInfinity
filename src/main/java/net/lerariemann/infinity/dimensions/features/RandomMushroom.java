package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomMushroom extends RandomisedFeature {
    public RandomMushroom(RandomFeaturesList parent, boolean placef) {
        super(parent, "mushroom", placef);
        id = random.nextBoolean() ? "huge_brown_mushroom" : "huge_red_mushroom";
        type = "tree";
        save(1 + random.nextInt(20), (int) Math.floor(random.nextExponential()*4), parent.surface_block);
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.put("cap_provider", PROVIDER.randomBlockProvider(random, "full_blocks"));
        config.put("stem_provider", PROVIDER.randomBlockProvider(random, "full_blocks"));
        config.putInt("foliage_radius", random.nextBoolean() ? 2 : 1 + (int)Math.floor(random.nextExponential()*2));
        return feature(config);
    }
}
