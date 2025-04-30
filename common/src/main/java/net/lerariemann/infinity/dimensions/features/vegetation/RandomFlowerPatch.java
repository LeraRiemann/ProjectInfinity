package net.lerariemann.infinity.dimensions.features.vegetation;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import static net.lerariemann.infinity.dimensions.features.Placement.matchingBlocks;
import static net.lerariemann.infinity.dimensions.features.Placement.singleRule;

public class RandomFlowerPatch extends RandomisedFeature {
    public RandomFlowerPatch(RandomFeaturesList parent) {
        super(parent, "flowers");
        id = "flower";
        savePlacement();
    }

    public NbtList placement() {
        Placement res = new Placement();
        int a = random.nextInt(1, 9);
        if (a>1) res.addCount(a);
        res.addRarityFilter(1 + random.nextInt(32));
        res.addInSquare();
        res.addHeightmap("MOTION_BLOCKING");
        res.addBiome();
        return res.data;
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.putInt("tries", 1 + random.nextInt( 100));
        config.putInt("xz_spread", random.nextInt(2, 13));
        config.putInt("y_spread", random.nextInt(1, 6));

        NbtList flowerlist = new NbtList();
        int i = 1;
        while (i > 0) {
            NbtCompound entry = new NbtCompound();
            addRandomBlock(entry, "data", ConfigType.FLOWERS);
            entry.putInt("weight", 1 + random.nextInt(20));
            flowerlist.add(entry);
            i = random.nextDouble() > 0.5 ? 1 : 0;
        }
        NbtList placement_inner = new NbtList();
        placement_inner.add(singleRule("block_predicate_filter", "predicate", matchingBlocks("minecraft:air")));
        NbtCompound config_inner = new NbtCompound();
        config_inner.put("to_place", singleRule("weighted_state_provider", "entries", flowerlist));
        NbtCompound feature_inner = singleRule("simple_block", "config", config_inner);
        NbtCompound feature_outer = new NbtCompound();
        feature_outer.put("feature", feature_inner);
        feature_outer.put("placement", placement_inner);
        config.put("feature", feature_outer);
        return feature(config);
    }
}
