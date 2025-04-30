package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomEndIsland extends RandomisedFeature {
    public RandomEndIsland(RandomFeaturesList parent) {
        super(parent, "island");
        id = "infinity:random_end_island";
        save_with_placement();
    }

    NbtCompound element(int data, int weight) {
        NbtCompound res = new NbtCompound();
        res.putInt("data", data);
        res.putInt("weight", weight);
        return res;
    }

    NbtCompound count() {
        NbtList distribution = new NbtList();
        int c = random.nextInt(2);
        distribution.add(element(1, 1 + random.nextInt(6)));
        distribution.add(element(2, 1 + random.nextInt(4)));
        if (c!=0) {
            distribution.add(element(3, c));
        }
        return singleRule("weighted_list", "distribution", distribution);
    }

    void placement() {
        RandomDimension dim = parent.parent.parent;
        int a = (int)random.nextGaussian(dim.sea_level, 16);
        int b = random.nextInt(dim.sea_level, dim.height + dim.min_y);
        addRarityFilter(1 + random.nextInt(16));
        addSingleRule("count", "count", count());
        addInSquare();
        addHeightRange(uniformHeightRange(Math.min(b, Math.max(a, dim.min_y)), Math.max(a, b)));
        addBiome();
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "state", ConfigType.BLOCKS_FEATURES);
        return feature(config);
    }
}
