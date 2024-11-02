package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.Arrays;

public class RandomCrop extends RandomisedFeature {
    NbtCompound cropp;
    boolean water;
    int start;

    public RandomCrop(RandomFeaturesList parent) {
        super(parent, "crop");
        id = "block_column";
        NbtElement crop = PROVIDER.extraRegistry.get("crops").getRandomElement(random);
        cropp = (NbtCompound)crop;
        start = daddy.sea_level - (cropp.getKeys().contains("offset") ? cropp.getInt("offset") : 1);
        water = cropp.getBoolean("needsWater");
        save_with_placement();
    }

    static NbtCompound blockToLayer(NbtCompound block) {
        NbtCompound layer = new NbtCompound();
        NbtCompound provider = RandomProvider.blockToProvider(block);
        layer.put("provider", provider);
        layer.putInt("height", 1);
        return layer;
    }

    void placement() {
        if (!water) {
            addCountEveryLayer(random.nextInt(16));
            NbtCompound toAdd = ofType("random_offset");
            toAdd.putInt("xz_spread", -1);
            toAdd.putInt("y_spread", 0);
            placement_data.add(toAdd);
            addBlockPredicateFilter(matchingBlocks(parent.surface_block.getString("Name")));
            addBiome();
        }
        NbtCompound value = new NbtCompound();
        value.putInt("absolute", start);
        NbtList predicates = new NbtList();
        Arrays.asList(Arrays.asList(-1, 0, 0),
                Arrays.asList(-1, 0, -1),
                Arrays.asList(-1, 0, 1),
                Arrays.asList(1, 0, -1),
                Arrays.asList(1, 0, 0),
                Arrays.asList(1, 0, 1),
                Arrays.asList(0, 0, 1),
                Arrays.asList(0, 0, -1)).forEach(a -> predicates.add(matchingWaterOffset(offsetToNbt((a)))));
        addCount(random.nextInt(16));
        addInSquare();
        addHeightRange(singleRule("constant", "value", value));
        addBlockPredicateFilter(not(matchingWater()));
        addBlockPredicateFilter(singleRule("any_of", "predicates", predicates));
        addBiome();
    }

    @Override
    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.putString("direction", "up");
        config.putBoolean("prioritize_tip", false);
        NbtList layers = new NbtList();
        boolean bl = cropp.getKeys().contains("blocks");
        for (int i = 0; bl ? (i < cropp.getInt("blocks")) : (cropp.getKeys().contains("block_" + i)); i++)
            layers.add(blockToLayer(cropp.getCompound("block_" + i)));
        layers.add(blockToLayer(RandomProvider.Block("minecraft:air")));
        config.put("layers", layers);
        NbtCompound allowedPlacement = new NbtCompound();
        allowedPlacement.putString("type", "true");
        config.put("allowed_placement", allowedPlacement);
        return feature(config);
    }
}
