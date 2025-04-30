package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.CorePack;
import net.minecraft.nbt.*;

public class RandomVegetation extends RandomisedFeature {
    boolean sparse;

    public RandomVegetation(RandomFeaturesList parent) {
        super(parent.parent.id, parent, "vegetation");
        sparse = PROVIDER.roll(random, "use_sparse_vegetation");
        id = "random_selector";
        save_with_placement();
    }

    void placement() {
        int a = 1 + random.nextInt(10);
        if (sparse) addRarityFilter(a);
        else addCount(a);
        addInSquare();
        addWaterDepthFilter((int) Math.floor(random.nextExponential()*4));
        addHeightmap("MOTION_BLOCKING");
        addBiome();
    }
    NbtElement randomTree() {
        String tree = ConfigType.TREES.getDef();
        if (parent.roll("use_vanilla_trees")) {
            tree = PROVIDER.randomName(random, ConfigType.TREES);
            NbtCompound c = CorePack.treePlacement(tree, parent.surface_block.getString("Name"));
            String s = tree.substring(tree.lastIndexOf(':') + 1).replace("/", "_") + "_" + parent.parent.id;
            s = s.replace("/", "_");
            CommonIO.write(c, parent.storagePath + "/worldgen/placed_feature", s + ".json");
            return NbtString.of(InfinityMod.MOD_ID + ":" + s);
        }
        else {
            switch(PROVIDER.randomName(random, ConfigType.FLORAL_DISTRIBUTION)) {
                case "fungi" -> tree = (new RandomFungus(parent)).fullName();
                case "mushrooms" -> tree = (new RandomMushroom(parent)).fullName();
                case "trees" -> tree = (new RandomTree(parent)).fullName();
            }
        }
        return NbtString.of(tree);
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.put("default", randomTree());
        NbtList features = new NbtList();
        int numtreetypes = sparse ? random.nextInt(6) : random.nextInt(1,4);
        for (int i = 0; i < numtreetypes; i++) {
            NbtCompound tree = new NbtCompound();
            tree.putFloat("chance", random.nextFloat());
            tree.put("feature", randomTree());
            features.add(tree);
        }
        config.put("features", features);
        return feature(config);
    }
}
