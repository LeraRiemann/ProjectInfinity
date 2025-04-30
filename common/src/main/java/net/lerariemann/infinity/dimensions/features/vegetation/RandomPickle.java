package net.lerariemann.infinity.dimensions.features.vegetation;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomPickle extends RandomisedFeature {
    public RandomPickle(RandomFeaturesList lst) {
        super(lst.parent.id, lst, "pickle");
        id = "minecraft:sea_pickle";
        savePlacement();
    }
    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.putInt("count", 1 + random.nextInt(30));
        return feature(config);
    }
    public NbtList placement() {
        Placement res = new Placement();
        res.addRarityFilter(4 + random.nextInt(20));
        res.addInSquare();
        res.addHeightmap("OCEAN_FLOOR_WG");
        res.addBiome();
        return res.data;
    }
}
