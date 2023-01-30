package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.dimensions.WeighedStructure;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.List;
import java.util.Objects;

public class RandomVegetation extends RandomisedFeature {
    WeighedStructure <String> trees_vanilla;
    List<String> blocks;
    String PATH;

    public RandomVegetation(int i, RandomProvider provider, String path, WeighedStructure<String> trees, List<String> top_blocks) {
        super(i, provider);
        trees_vanilla = trees;
        name = "vegetation_" + i;
        id = "random_selector";
        type = "vegetation";
        PATH = path;
        blocks = top_blocks;
        save(path,1 + random.nextInt(10));
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
                    tree = (new RandomFungus(random.nextInt(), PROVIDER, PATH, blocks)).fullName();
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
