package net.lerariemann.infinity.dimensions.features.surface_structures;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomWell extends RandomisedFeature {
    public RandomWell(RandomFeaturesList lst) {
        super(lst, "well");

        NbtCompound moredata = new NbtCompound();
        moredata.putString("feature", "minecraft:desert_well");
        moredata.put("placement", placement());
        CommonIO.write(moredata, parent.storagePath + "/worldgen/placed_feature", name + ".json");
    }

    public NbtCompound feature() {
        return null;
    }
    public NbtList placement() {
        Placement res = new Placement();
        res.addRarityFilter(random.nextInt(1200));
        res.addInSquare();
        res.addHeightmap("MOTION_BLOCKING");
        res.addBiome();
        return res.data;
    }
}
