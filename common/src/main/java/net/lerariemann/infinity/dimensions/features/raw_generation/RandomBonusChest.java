package net.lerariemann.infinity.dimensions.features.raw_generation;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomBonusChest extends RandomisedFeature {
    public RandomBonusChest(RandomFeaturesList lst) {
        super(lst, "chest");
        id = "infinity:bonus_chest";
        savePlacement();
    }

    @Override
    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.putString("loot", PROVIDER.randomName(random, ConfigType.LOOT_TABLES));
        return feature(config);
    }

    @Override
    public NbtList placement() {
        Placement res = new Placement();
        res.addCount(random.nextInt(1, 16));
        res.addBiome();
        return res.data;
    }
}
