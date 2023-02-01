package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.WeighedStructure;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.List;
import java.util.Objects;

public class RandomVegetation extends RandomisedFeature {
    WeighedStructure <String> trees_vanilla;
    List<String> validbaseblocks;
    String mainsurfaceblock;
    String PATH;

    public RandomVegetation(RandomFeaturesList parent) {
        super(parent.parent.id, parent.PROVIDER);
        trees_vanilla = parent.trees;
        name = "vegetation_" + parent.parent.id;
        id = "random_selector";
        type = "vegetation";
        PATH = parent.storagePath;
        validbaseblocks = parent.blocks;
        mainsurfaceblock = parent.surface_block;
        save(PATH,1 + random.nextInt(10));
    }

    NbtElement randomTree() {
        String tree = "minecraft:oak";
        if (random.nextBoolean()) {
            tree = trees_vanilla.getRandomElement(random);
            if (!Objects.equals(tree, "minecraft:azalea_tree")) return NbtString.of(tree);
        }
        else {
            switch(random.nextInt(2)) {
                case 0 -> {
                    tree = (new RandomFungus(random.nextInt(), PROVIDER, PATH, validbaseblocks, mainsurfaceblock)).fullName();
                    return NbtString.of(tree);
                }
                case 1 -> tree = (new RandomMushroom(random.nextInt(), PROVIDER, PATH)).fullName();
            }
        }
        NbtCompound placedfeature = new NbtCompound();
        placedfeature.putString("feature", tree);
        placedfeature.put("placement", new NbtList());
        return placedfeature;
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
