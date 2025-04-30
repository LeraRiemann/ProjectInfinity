package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.nbt.NbtCompound;

public class RandomWell extends RandomisedFeature {
    public RandomWell(RandomFeaturesList lst) {
        super(lst, "well");

        NbtCompound moredata = new NbtCompound();
        moredata.putString("feature", "minecraft:desert_well");
        placement();
        moredata.put("placement", placement_data);
        CommonIO.write(moredata, parent.storagePath + "/worldgen/placed_feature", name + ".json");
    }

    @Override
    NbtCompound feature() {
        return null;
    }

    @Override
    void placement() {
        addRarityFilter(random.nextInt(1200));
        addInSquare();
        addHeightmap("MOTION_BLOCKING");
        addBiome();
    }
}
