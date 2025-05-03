package net.lerariemann.infinity.dimensions.features.vegetation;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomSurfacePatch extends RandomisedFeature {
    public RandomSurfacePatch(RandomFeaturesList parent) {
        super(parent, "patch");
        id = "random_patch";
        savePlacement();
    }

    public NbtList placement() {
        Placement res = new Placement();
        res.addRarityFilter(1 + random.nextInt(64));
        res.addInSquare();
        res.addBiome();
        return res.data;
    }

    public NbtCompound feature() {
        int xz_spread = 2 + random.nextInt(14);
        int y_spread = 1 + random.nextInt(7);
        int tries_max = (xz_spread+1)*(xz_spread+1);
        NbtCompound config = CommonIO.readAndFormat(InfinityMod.utilPath + "/preplacements/surfacepatch.json",
                CommonIO.compoundToString(PROVIDER.randomElement(random, ConfigType.BLOCKS_FEATURES)),
                parent.surface_block.getString("Name"), Math.min(256, random.nextInt(tries_max)), xz_spread, y_spread);
        return feature(config);
    }
}
