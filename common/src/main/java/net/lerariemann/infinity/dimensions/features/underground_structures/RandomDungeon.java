package net.lerariemann.infinity.dimensions.features.underground_structures;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomDungeon extends RandomisedFeature {
    public RandomDungeon(RandomFeaturesList parent) {
        super(parent, "dungeon");
        id = "infinity:random_dungeon";
        savePlacement();
    }

    public NbtList placement() {
        return Placement.uniform(10 + random.nextInt(200));
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "main_state", ConfigType.FULL_BLOCKS);
        addRandomBlock(config, "decor_state", ConfigType.FULL_BLOCKS_WG);
        config.putString("mob", PROVIDER.randomName(random, ConfigType.MOBS));
        config.putInt("size", 1 + random.nextInt(6));
        return feature(config);
    }
}
