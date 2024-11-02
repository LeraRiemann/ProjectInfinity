package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomCeilingBlob extends RandomisedFeature {
    public RandomCeilingBlob(RandomFeaturesList parent) {
        super(parent, "ceilingblob");
        id = "infinity:random_ceiling_blob";
        save_with_placement();
    }

    void placement() {
        addCount(random.nextInt(1, daddy.height/8));
        addInSquare();
        addHeightRange(fullHeightRange());
        addBiome();
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtList targets = new NbtList();
        targets.add(daddy.default_block);
        targets.addAll(daddy.additional_blocks);
        config.put("targets", targets);
        addRandomBlockProvider(config, "block", "full_blocks");
        config.putInt("size_xz", random.nextInt(5, 12));
        config.putInt("size_y", random.nextInt(8, 16));
        return feature(config);
    }
}
