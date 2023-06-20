package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.WeighedStructure;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.Objects;

public class RandomVegetation extends RandomisedFeature {
    WeighedStructure <String> trees_vanilla;

    public RandomVegetation(RandomFeaturesList parent) {
        super(parent.parent.id, parent, "vegetation");
        trees_vanilla = parent.trees;
        id = "random_selector";
        type = "vegetation";
        save(1 + random.nextInt(10), (int) Math.floor(random.nextExponential()*4));
    }

    NbtElement randomTree() {
        String tree = "minecraft:oak";
        if (Objects.equals(parent.surface_block, "minecraft:grass_block") && parent.roll("use_vanilla_trees")) {
            tree = trees_vanilla.getRandomElement(random);
            return NbtString.of(tree);
        }
        else {
            switch(PROVIDER.floralDistribution.getRandomElement(random)) {
                case "fungi" -> tree = (new RandomFungus(parent)).fullName();
                case "mushrooms" -> tree = (new RandomMushroom(parent, true)).fullName();
                case "trees" -> tree = (new RandomTree(parent, true)).fullName();
            }
        }
        return NbtString.of(tree);
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.put("default", randomTree());
        NbtList features = new NbtList();
        int numtreetypes = 1 +random.nextInt(6);
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
