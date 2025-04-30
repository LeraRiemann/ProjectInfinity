package net.lerariemann.infinity.dimensions.features.raw_generation;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import static net.lerariemann.infinity.dimensions.features.Placement.*;

public class RandomEndIsland extends RandomisedFeature {
    public RandomEndIsland(RandomFeaturesList parent) {
        super(parent, "island");
        id = "infinity:random_end_island";
        savePlacement();
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

    public NbtList placement() {
        Placement res = new Placement();
        int a = (int)random.nextGaussian(daddy.sea_level, 16);
        int b = random.nextInt(daddy.sea_level, daddy.height + daddy.min_y);
        res.addRarityFilter(1 + random.nextInt(16));
        res.addSingleRule("count", "count", count());
        res.addInSquare();
        res.addHeightRange(uniformHeightRange(Math.min(b, Math.max(a, daddy.min_y)), Math.max(a, b)));
        res.addBiome();
        return res.data;
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "state", ConfigType.BLOCKS_FEATURES);
        return feature(config);
    }
}
