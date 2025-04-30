package net.lerariemann.infinity.dimensions.features.vegetation;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomSeagrass extends RandomisedFeature {
    public RandomSeagrass(RandomFeaturesList lst) {
        super(lst.parent.id, lst, "seagrass");
        id = "minecraft:seagrass";
        savePlacement();
    }
    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.putDouble("probability", random.nextDouble());
        return feature(config);
    }
    public NbtList placement() {
        Placement res = new Placement();
        res.addInSquare();
        res.addHeightmap("OCEAN_FLOOR_WG");
        res.addCount(random.nextInt(10, 100));
        res.addBiome();
        return res.data;
    }
}
