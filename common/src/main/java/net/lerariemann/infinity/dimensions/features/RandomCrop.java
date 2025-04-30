package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.Arrays;

@Deprecated
public class RandomCrop extends RandomisedFeature {
    NbtCompound crop;
    boolean water;
    int start;

    public RandomCrop(RandomFeaturesList parent) {
        super(parent, "crop");
        id = "block_column";
        //crop = PROVIDER.randomElement(random, "crops");
        start = daddy.sea_level - (crop.getKeys().contains("offset") ? crop.getInt("offset") : 1);
        water = crop.getBoolean("needsWater");
        save_with_placement();
    }

    static NbtCompound blockToLayer(NbtCompound block) {
        NbtCompound layer = new NbtCompound();
        NbtCompound provider = NbtUtils.blockToSimpleStateProvider(block);
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
        boolean bl = crop.getKeys().contains("blocks");
        for (int i = 0; bl ? (i < crop.getInt("blocks")) : (crop.getKeys().contains("block_" + i)); i++)
            layers.add(blockToLayer(crop.getCompound("block_" + i)));
        layers.add(blockToLayer(NbtUtils.nameToElement("minecraft:air")));
        config.put("layers", layers);
        NbtCompound allowedPlacement = new NbtCompound();
        allowedPlacement.putString("type", "true");
        config.put("allowed_placement", allowedPlacement);
        return feature(config);
    }
}
